package com.example.statement_detect.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.statement_detect.R
import com.example.statement_detect.timer.TimerStatus

class TimerViewModel : ViewModel() {

    // --- 对外暴露的状态 ---
    var timerStatus by mutableStateOf(TimerStatus.PAUSED)
        private set
    var lastRunStatus by mutableStateOf(TimerStatus.WORKING)
        private set

    var scheduledWorkTimeInSeconds by mutableStateOf(0)
        private set
    var scheduledRelaxTimeInSeconds by mutableStateOf(0)
        private set
    var currentWorkTimeInSeconds by mutableStateOf(0)
        private set
    var currentRelaxTimeInSeconds by mutableStateOf(0)
        private set
    var round by mutableStateOf(0)
        private set
    var photoTimeList by mutableStateOf<List<Int>>(emptyList())
        private set

    // 拍照触发回调，由外部设置
    var onTriggerPhoto: (() -> Unit)? = null

    // --- 音效 ---
    private var soundPool: SoundPool? = null
    private var workEndSoundId = 0
    private var workStartSoundId = 0
    private var workFlowEndSoundId = 0

    // --- 计时器 Handler ---
    private val handler = Handler(Looper.getMainLooper())
    private val segments = 5

    private val countDownRunnable = object : Runnable {
        override fun run() {
            when (timerStatus) {
                TimerStatus.WORKING -> {
                    if (currentWorkTimeInSeconds > 0) {
                        currentWorkTimeInSeconds--
                        if (currentWorkTimeInSeconds in photoTimeList) {
                            onTriggerPhoto?.invoke()
                        }
                    } else {
                        timerStatus = TimerStatus.RELAXING
                        currentWorkTimeInSeconds = scheduledWorkTimeInSeconds
                        soundPool?.play(workEndSoundId, 1f, 1f, 0, 0, 1f)
                    }
                }
                TimerStatus.RELAXING -> {
                    if (currentRelaxTimeInSeconds > 0) {
                        currentRelaxTimeInSeconds--
                    } else {
                        if (round > 0) {
                            round--
                            timerStatus = TimerStatus.WORKING
                            photoTimeList = getPhotoPoints(scheduledWorkTimeInSeconds, segments)
                            soundPool?.play(workStartSoundId, 1f, 1f, 0, 0, 1f)
                        } else {
                            timerStatus = TimerStatus.PAUSED
                            lastRunStatus = TimerStatus.WORKING
                            photoTimeList = getPhotoPoints(scheduledWorkTimeInSeconds, segments)
                            soundPool?.play(workFlowEndSoundId, 1f, 1f, 0, 0, 1f)
                        }
                        currentRelaxTimeInSeconds = scheduledRelaxTimeInSeconds
                    }
                }
                TimerStatus.PAUSED -> { /* 不做任何事 */ }
            }
            handler.postDelayed(this, 1000)
        }
    }

    // --- 初始化音效，需要 Context，在第一次进入 Composable 时调用 ---
    fun initSounds(context: Context) {
        if (soundPool != null) return // 防止重复初始化
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
        workEndSoundId = soundPool!!.load(context, R.raw.work_end_sound_effect, 1)
        workStartSoundId = soundPool!!.load(context, R.raw.bell_sound, 1)
        workFlowEndSoundId = soundPool!!.load(context, R.raw.workflow_end_sound, 1)
        handler.post(countDownRunnable)
    }

    // --- 对外操作函数 ---
    fun togglePlayPause() {
        if (timerStatus == TimerStatus.PAUSED) {
            timerStatus = lastRunStatus
            if (timerStatus == TimerStatus.WORKING) {
                soundPool?.play(workStartSoundId, 1f, 1f, 0, 0, 1f)
            }
        } else {
            lastRunStatus = timerStatus
            timerStatus = TimerStatus.PAUSED
        }
    }

    fun incrementRound() {
        if (timerStatus == TimerStatus.PAUSED) round = (round + 1) % 10
    }

    fun decrementRound() {
        if (timerStatus == TimerStatus.PAUSED) round = (round + 9) % 10
    }

    fun incrementWorkTime() {
        if (timerStatus == TimerStatus.PAUSED) {
            scheduledWorkTimeInSeconds = handleTime(scheduledWorkTimeInSeconds, isAdd = true, isMinus = false)
            currentWorkTimeInSeconds = scheduledWorkTimeInSeconds
            photoTimeList = getPhotoPoints(scheduledWorkTimeInSeconds, segments)
        }
    }

    fun decrementWorkTime() {
        if (timerStatus == TimerStatus.PAUSED) {
            scheduledWorkTimeInSeconds = handleTime(scheduledWorkTimeInSeconds, isAdd = false, isMinus = true)
            currentWorkTimeInSeconds = scheduledWorkTimeInSeconds
            photoTimeList = getPhotoPoints(scheduledWorkTimeInSeconds, segments)
        }
    }

    fun setWorkTime(totalSeconds: Int) {
        if (timerStatus == TimerStatus.PAUSED) {
            scheduledWorkTimeInSeconds = totalSeconds
            currentWorkTimeInSeconds = totalSeconds
            photoTimeList = getPhotoPoints(totalSeconds, segments)
        }
    }

    fun incrementRelaxTime() {
        if (timerStatus == TimerStatus.PAUSED) {
            scheduledRelaxTimeInSeconds = handleTime(scheduledRelaxTimeInSeconds, isAdd = true, isMinus = false)
            currentRelaxTimeInSeconds = scheduledRelaxTimeInSeconds
        }
    }

    fun decrementRelaxTime() {
        if (timerStatus == TimerStatus.PAUSED) {
            scheduledRelaxTimeInSeconds = handleTime(scheduledRelaxTimeInSeconds, isAdd = false, isMinus = true)
            currentRelaxTimeInSeconds = scheduledRelaxTimeInSeconds
        }
    }

    fun setRelaxTime(totalSeconds: Int) {
        if (timerStatus == TimerStatus.PAUSED) {
            scheduledRelaxTimeInSeconds = totalSeconds
            currentRelaxTimeInSeconds = totalSeconds
        }
    }

    // --- ViewModel 销毁时释放资源 ---
    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
        soundPool?.release()
        soundPool = null
    }
}