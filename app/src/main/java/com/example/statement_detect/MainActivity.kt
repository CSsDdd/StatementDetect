package com.example.statement_detect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Transition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.statement_detect.ui.theme.Statement_DetectTheme
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Statement_DetectTheme {
                var NeedPhoto by remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ClockGUI(modifier = Modifier.padding(innerPadding)){
                        NeedPhoto=true
                    }
                    RequestCameraPermission(modifier = Modifier.padding(innerPadding),shouldTakePhoto = NeedPhoto)
                }
            }
        }
    }
}

fun handleTime(totalSeconds: Int, isAdd: Boolean, isMinus: Boolean): Int {
    val dayInSeconds = 86400
    var newTotalSeconds = totalSeconds
    if (isAdd) {
        newTotalSeconds++
    } else if (isMinus) {
        newTotalSeconds--
    }
    return (newTotalSeconds + dayInSeconds) % dayInSeconds
}


enum class TimerStatus {
    PAUSED,      // 暂停
    WORKING,     // 工作倒计时
    RELAXING     // 休息倒计时
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    totalSeconds: Int,
    onDismiss: (Int) -> Unit
) {
    val bufferCount = 3 // 顶部/底部的空白项数量

    // 构建带空白项的列表
    val hourItems = remember { List(bufferCount) { null } + (0..23).toList() + List(bufferCount) { null } }
    val minuteItems = remember { List(bufferCount) { null } + (0..59).toList() + List(bufferCount) { null } }
    val secondItems = remember { List(bufferCount) { null } + (0..59).toList() + List(bufferCount) { null } }

    // 目标数值在列表中的实际索引（加上 bufferCount）
    val targetHourIndex = totalSeconds / 3600
    val targetMinuteIndex = ( totalSeconds / 60) % 60
    val targetSecondIndex = totalSeconds % 60

    // 滚筒状态，初始索引设为目标索引
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = targetHourIndex)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = targetMinuteIndex)
    val secondState = rememberLazyListState(initialFirstVisibleItemIndex = targetSecondIndex)

    val density = LocalDensity.current
    val columnHeight = 200.dp      // 与 WheelPickerColumn 中一致
    val itemHeight = 48.dp
    val targetOffsetPx = with(density) { ((columnHeight - itemHeight) / 2).roundToPx() }

    // 组合后立即滚动到目标索引并使其居中
    LaunchedEffect(Unit) {
        launch { hourState.scrollToItem(targetHourIndex, targetOffsetPx) }
        launch { minuteState.scrollToItem(targetMinuteIndex, targetOffsetPx) }
        launch { secondState.scrollToItem(targetSecondIndex, targetOffsetPx) }
    }

    Dialog(
        onDismissRequest = {
            // 计算每个滚筒当前居中项的索引
            val hourCenterIndex = getCenterItemIndex(hourState, bufferCount, 0..23)
            val minuteCenterIndex = getCenterItemIndex(minuteState, bufferCount, 0..59)
            val secondCenterIndex = getCenterItemIndex(secondState, bufferCount, 0..59)
            onDismiss(hourCenterIndex * 3600 + minuteCenterIndex * 60 + secondCenterIndex)
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPickerColumn(
                    items = hourItems,
                    state = hourState,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    ":",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.digital7_mono))
                )
                WheelPickerColumn(
                    items = minuteItems,
                    state = minuteState,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    ":",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.digital7_mono))
                )
                WheelPickerColumn(
                    items = secondItems,
                    state = secondState,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** 获取当前滚筒中距离可视区域中心最近的数值项索引（原始值，已减去 bufferCount） */
private fun getCenterItemIndex(
    state: LazyListState,
    bufferCount: Int,
    validRange: IntRange
): Int {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return validRange.first

    val viewportCenter = layoutInfo.viewportEndOffset / 2f
    // 找到最接近视口中心的项
    val closestItem = visibleItems.minByOrNull { item ->
        val itemCenter = item.offset + item.size / 2f
        kotlin.math.abs(itemCenter - viewportCenter)
    } ?: return validRange.first

    val rawIndex = closestItem.index
    // 减去 bufferCount，并限制在有效范围内
    return (rawIndex - bufferCount).coerceIn(validRange.first, validRange.last)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPickerColumn(
    items: List<Int?>,        // 允许 null 表示空白项
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    val itemHeight = 48.dp
    val columnHeight = 200.dp

    LazyColumn(
        state = state,
        flingBehavior = rememberSnapFlingBehavior(state, SnapPosition.Center), // 居中吸附
        modifier = modifier
            .height(columnHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items) { value ->
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (value != null) {
                    Text(
                        text = "%02d".format(value),
                        fontFamily = FontFamily(Font(R.font.digital7_mono)),
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                } else {
                    // 空白项：不显示数字，保持占位
                    Spacer(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
fun get_Photo_Points(totalSeconds: Int,Segments: Int): MutableList<Int>{
    var ResTime = MutableList<Int>(1){ 0 }
    var SegmentLen=totalSeconds/Segments
    for (i in 1 until Segments) {
        ResTime.add(SegmentLen*i+ Random.nextInt((-SegmentLen*0.4).toInt(),(SegmentLen*0.4).toInt()))
    }
    return ResTime
}
@Composable
fun ClockGUI(modifier: Modifier = Modifier,
             Segments:Int = 2,
             onTriggerPhoto: () -> Unit = {},) {
    var scheduledWorkTimeInSeconds: Int by remember { mutableStateOf(0) }
    var scheduledRelaxTimeInSeconds: Int by remember { mutableStateOf(0) }
    var currentWorkTimeInSeconds: Int by remember { mutableStateOf(0) }
    var currentRelaxTimeInSeconds: Int by remember { mutableStateOf(0) }
    var round: Int by remember { mutableStateOf(0) }
    var roundsHeight by remember { mutableStateOf(0) }
    var playButtonHeight by remember { mutableStateOf(0) }
    var timerStatus: TimerStatus by remember { mutableStateOf(TimerStatus.PAUSED) }
    var lastRunStatus: TimerStatus by remember { mutableStateOf(TimerStatus.WORKING) }
    val context = LocalContext.current
    var photoTimeList = remember { MutableList<Int>(Segments){0} }

    val audioAttributes = remember{ AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    }

    var soundPool = remember{ SoundPool.Builder()
        .setMaxStreams(3) // 最多同时播放3个音效
        .setAudioAttributes(audioAttributes)
        .build()
    }

    val WorkEndSoundId = remember{ soundPool?.load(context, R.raw.work_end_sound_effect, 1) ?: 0 }
    val WorkStartSoundId = remember{ soundPool?.load(context, R.raw.bell_sound, 1) ?: 0 }
    val WorkFlowEndSoundId= remember{ soundPool?.load(context, R.raw.workflow_end_sound, 1) ?: 0 }
    val soundsLoaded = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())

        soundPool.setOnLoadCompleteListener { _, _, status ->
            soundsLoaded.value = (status == 0)
        }//音效库初始化

        val countDownRunnable = object : Runnable {
            override fun run() {
                when (timerStatus) {
                    TimerStatus.WORKING -> {
                        if (currentWorkTimeInSeconds > 0) {
                            currentWorkTimeInSeconds--
                            if(currentWorkTimeInSeconds in photoTimeList){
                            }
                        } else {
                            timerStatus = TimerStatus.RELAXING
                            currentWorkTimeInSeconds = scheduledWorkTimeInSeconds
                            soundPool.play(WorkEndSoundId,1f,1f,0,0,1f)
                        }
                    }

                    TimerStatus.RELAXING -> {
                        if (currentRelaxTimeInSeconds > 0) {
                            currentRelaxTimeInSeconds--
                        } else {
                            if (round > 0) {
                                round--
                                timerStatus = TimerStatus.WORKING
                                soundPool.play(WorkStartSoundId,1f,1f,0,0,1f)
                            } else {
                                timerStatus = TimerStatus.PAUSED
                                soundPool.play(WorkFlowEndSoundId,1f,1f,0,0,1f)
                            }
                            currentRelaxTimeInSeconds = scheduledRelaxTimeInSeconds

                        }
                    }

                    TimerStatus.PAUSED -> {
                        // Do nothing
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }//倒计时逻辑初始化

        handler.post(countDownRunnable)

        onDispose {
            handler.removeCallbacksAndMessages(null)//释放handler内存
            soundPool.release()//释放音效内存
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF010B18),
                            Color(0xFF01122C),
                            Color(0xFF022150),
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 6.dp,
                    color = Color(0xFF3A4249),
                    shape = CircleShape
                )
                .align(Alignment.Center), contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .onGloballyPositioned { coordinates ->
                            roundsHeight = coordinates.size.height
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Rounds",
                        color = Color.White,
                    )
                    val roundColor = if (timerStatus != TimerStatus.PAUSED) Color(0xFF83D086) else Color.White
                    Text(
                        text = "%01d".format(round),
                        fontFamily = FontFamily(Font(R.font.digital7_mono)),
                        fontSize = 30.sp,
                        color = roundColor
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (timerStatus == TimerStatus.PAUSED) {
                                    round = (round + 1) % 10
                                }
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Blue)
                                .fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Round Up",
                                tint = Color.White,
                            )
                        }

                        IconButton(
                            onClick = {
                                if (timerStatus == TimerStatus.PAUSED) {
                                    round = (round + 9) % 10
                                }
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Blue)
                                .fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Round Down",
                                tint = Color.White,
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Work Time",
                            color = Color.White,
                        )
                        val workH = currentWorkTimeInSeconds / 3600
                        val workM = (currentWorkTimeInSeconds % 3600) / 60
                        val workS = currentWorkTimeInSeconds % 60
                        val workTimeColor = if (timerStatus == TimerStatus.WORKING) Color(0xFF83D086) else Color.White
                        var showWorkTimeSelector:Boolean by remember { mutableStateOf(false) }
                        Text(
                            text = "%02d:%02d:%02d".format(workH, workM, workS),
                            fontFamily = FontFamily(Font(R.font.digital7_mono)),
                            fontSize = 30.sp,
                            color = workTimeColor,
                            modifier = Modifier.clickable{
                                if(timerStatus==TimerStatus.PAUSED){
                                    showWorkTimeSelector=true
                                }
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    if (timerStatus == TimerStatus.PAUSED) {
                                        scheduledWorkTimeInSeconds = handleTime(scheduledWorkTimeInSeconds, isAdd = true, isMinus = false)
                                        currentWorkTimeInSeconds = scheduledWorkTimeInSeconds
                                    }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Work Time Up",
                                    tint = Color.White,
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (timerStatus == TimerStatus.PAUSED) {
                                        scheduledWorkTimeInSeconds = handleTime(scheduledWorkTimeInSeconds, isAdd = false, isMinus = true)
                                        currentWorkTimeInSeconds = scheduledWorkTimeInSeconds
                                    }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Work Time Down",
                                    tint = Color.White,
                                )
                            }
                        }
                        if(showWorkTimeSelector==true){
                            TimePickerDialog(modifier = Modifier, totalSeconds = scheduledWorkTimeInSeconds) {
                                res->
                                scheduledWorkTimeInSeconds=res
                                currentWorkTimeInSeconds=scheduledWorkTimeInSeconds
                                showWorkTimeSelector=false
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Relax Time",
                            color = Color.White,
                        )
                        val relaxH = currentRelaxTimeInSeconds / 3600
                        val relaxM = (currentRelaxTimeInSeconds % 3600) / 60
                        val relaxS = currentRelaxTimeInSeconds % 60
                        val relaxTimeColor = if (timerStatus == TimerStatus.RELAXING) Color(0xFF83D086) else Color.White
                        var showRelaxTimeSelector : Boolean by remember { mutableStateOf(false) }
                        Text(
                            text = "%02d:%02d:%02d".format(relaxH, relaxM, relaxS),
                            fontFamily = FontFamily(Font(R.font.digital7_mono)),
                            fontSize = 30.sp,
                            color = relaxTimeColor,
                            modifier = Modifier.clickable{
                                if(timerStatus == TimerStatus.PAUSED){
                                    showRelaxTimeSelector=true
                                }
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    if (timerStatus == TimerStatus.PAUSED) {
                                        scheduledRelaxTimeInSeconds = handleTime(scheduledRelaxTimeInSeconds, isAdd = true, isMinus = false)
                                        currentRelaxTimeInSeconds = scheduledRelaxTimeInSeconds
                                    }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Relax Time Up",
                                    tint = Color.White,
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (timerStatus == TimerStatus.PAUSED) {
                                        scheduledRelaxTimeInSeconds = handleTime(scheduledRelaxTimeInSeconds, isAdd = false, isMinus = true)
                                        currentRelaxTimeInSeconds = scheduledRelaxTimeInSeconds
                                    }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Relax Time Down",
                                    tint = Color.White,
                                )
                            }
                            if(showRelaxTimeSelector==true){
                                TimePickerDialog(modifier = Modifier, totalSeconds = scheduledRelaxTimeInSeconds) {
                                        res->
                                    scheduledRelaxTimeInSeconds=res
                                    currentRelaxTimeInSeconds=scheduledRelaxTimeInSeconds
                                    showRelaxTimeSelector=false
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(with(LocalDensity.current) {
                    (roundsHeight - playButtonHeight).toDp()
                }))

                IconButton(
                    onClick = {
                        if (timerStatus == TimerStatus.PAUSED) {
                            timerStatus = lastRunStatus
                            if(timerStatus == TimerStatus.WORKING){
                                soundPool.play(WorkStartSoundId, 1f, 1f, 0, 0, 1f)
                            }
                        } else {
                            lastRunStatus = timerStatus
                            timerStatus = TimerStatus.PAUSED
                        }
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Blue)
                        .onGloballyPositioned { coordinates ->
                            playButtonHeight = coordinates.size.height
                        }
                ) {
                    val iconToShow = if (timerStatus == TimerStatus.PAUSED) {
                        Icons.Default.PlayArrow
                    } else {
                        ImageVector.vectorResource(id = R.drawable.baseline_pause_24)
                    }
                    Icon(
                        imageVector = iconToShow,
                        contentDescription = if (timerStatus == TimerStatus.PAUSED) "Play" else "Pause",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCameraPermission(modifier: Modifier = Modifier,shouldTakePhoto: Boolean = false) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        StartCamera(modifier = modifier,shouldTakePhoto=shouldTakePhoto)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("需要相机权限才能拍照")
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("授予权限")
            }
        }
    }
}

@Composable
fun StartCamera(modifier: Modifier,shouldTakePhoto: Boolean = false) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val preview = remember { androidx.camera.core.Preview.Builder().build() }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()
        val imageCaptureUseCase = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build()
        imageCapture = imageCaptureUseCase
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageCaptureUseCase,
                preview
            )
        } catch (exc: Exception) {
            Log.e("CameraCapture", "摄像头绑定失败", exc)
        }
    }
    Box {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            var imgToShow: Bitmap? by remember { mutableStateOf(null) }
            var isAtWork: Boolean by remember { mutableStateOf(false) }
            if (shouldTakePhoto) {
                captureUserPhoto(imageCapture, context) { bitmap ->
                    imgToShow = bitmap
                }
            }
            if (imgToShow != null) {
                ShowReminderWithPhoto(
                    image = imgToShow!!,
                ) {
                        res ->
                    isAtWork = res
                    imgToShow = null
                }
            }
        }

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    background = null
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    preview.setSurfaceProvider(surfaceProvider)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .requiredSize(180.dp, 240.dp)
        )
    }
}

fun captureUserPhoto(
    imageCapture: ImageCapture?,
    context: Context,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val imageCaptureInstance = imageCapture ?: return
    imageCaptureInstance.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "拍照失败: ${exception.message}", exception)
            }

            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                Log.d("Camera", "成功拍摄用户照片: ${bitmap.width}x${bitmap.height}")
                onPhotoCaptured(bitmap)
                image.close()
            }
        }
    )
}

@Composable
fun ShowReminderWithPhoto(image: Bitmap, onDismiss: (Boolean) -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss(false) },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageBitmap: ImageBitmap = image.asImageBitmap()
            Image(bitmap = imageBitmap, contentDescription = "captured")
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { onDismiss(true) }) {
                    Text("在工作")
                }
                Button(onClick = { onDismiss(false) }) {
                    Text("不在工作")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Statement_DetectTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ClockGUI(modifier = Modifier.padding(innerPadding))
            StartCamera(modifier = Modifier.padding(innerPadding))
        }
    }
}