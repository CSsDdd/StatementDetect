package com.example.statement_detect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.statement_detect.R
import com.example.statement_detect.timer.TimerStatus
import com.example.statement_detect.timer.TimerViewModel
import com.example.statement_detect.timer.TimerViewModelFactory
import androidx.compose.runtime.collectAsState

@Composable
fun ClockGUI(
    modifier: Modifier = Modifier,
    onTriggerPhoto: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: TimerViewModel = viewModel(
        factory = TimerViewModelFactory(context = context)
    )

    // 初始化音效和计时器（只执行一次）
    DisposableEffect(Unit) {
        vm.onTriggerPhoto = onTriggerPhoto
        vm.initSounds(context)
        onDispose {
            vm.onTriggerPhoto = null
        }
    }

    var roundsHeight by remember { mutableStateOf(0) }
    var playButtonHeight by remember { mutableStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
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
                .border(width = 6.dp, color = Color(0xFF3A4249), shape = CircleShape)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RoundsControl(
                    vm = vm,
                    onMeasured = { roundsHeight = it }
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    WorkTimeControl(vm = vm, modifier = Modifier.fillMaxWidth(0.5f))
                    RelaxTimeControl(vm = vm, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(with(LocalDensity.current) {
                    (roundsHeight - playButtonHeight).toDp()
                }))

                PlayPauseButton(
                    vm = vm,
                    onMeasured = { playButtonHeight = it }
                )
            }
        }
    }
}

// ---- 子组件 ----

@Composable
private fun RoundsControl(vm: TimerViewModel, onMeasured: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .onGloballyPositioned { onMeasured(it.size.height) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Rounds", color = Color.White)
        val roundColor = if (vm.timerStatus != TimerStatus.PAUSED) Color(0xFF83D086) else Color.White
        Text(
            text = "%01d".format(vm.currentRound),
            fontFamily = FontFamily(Font(R.font.digital7_mono)),
            fontSize = 30.sp,
            color = roundColor
        )
        Row(modifier = Modifier.fillMaxWidth(0.5f), horizontalArrangement = Arrangement.SpaceBetween) {
            SmallIconButton(icon = Icons.Default.KeyboardArrowUp, description = "Round Up") { vm.incrementRound() }
            SmallIconButton(icon = Icons.Default.KeyboardArrowDown, description = "Round Down") { vm.decrementRound() }
        }
    }
}

@Composable
private fun WorkTimeControl(vm: TimerViewModel, modifier: Modifier) {
    var showSelector by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Work Time", color = Color.White)
        val h = vm.currentWorkTimeInSeconds / 3600
        val m = (vm.currentWorkTimeInSeconds % 3600) / 60
        val s = vm.currentWorkTimeInSeconds % 60
        val color = if (vm.timerStatus == TimerStatus.WORKING) Color(0xFF83D086) else Color.White
        Text(
            text = "%02d:%02d:%02d".format(h, m, s),
            fontFamily = FontFamily(Font(R.font.digital7_mono)),
            fontSize = 30.sp,
            color = color,
            modifier = Modifier.clickable { if (vm.timerStatus == TimerStatus.PAUSED) showSelector = true }
        )
        Row(modifier = Modifier.fillMaxWidth(0.5f), horizontalArrangement = Arrangement.SpaceBetween) {
            SmallIconButton(Icons.Default.KeyboardArrowUp, "Work Time Up") { vm.incrementWorkTime() }
            SmallIconButton(Icons.Default.KeyboardArrowDown, "Work Time Down") { vm.decrementWorkTime() }
        }
        if (showSelector) {
            TimePickerDialog(totalSeconds = vm.scheduledWorkTimeInSeconds.collectAsState().value) { res ->
                vm.setWorkTime(res)
                showSelector = false
            }
        }
    }
}

@Composable
private fun RelaxTimeControl(vm: TimerViewModel, modifier: Modifier) {
    var showSelector by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Relax Time", color = Color.White)
        val h = vm.currentRelaxTimeInSeconds / 3600
        val m = (vm.currentRelaxTimeInSeconds % 3600) / 60
        val s = vm.currentRelaxTimeInSeconds % 60
        val color = if (vm.timerStatus == TimerStatus.RELAXING) Color(0xFF83D086) else Color.White
        Text(
            text = "%02d:%02d:%02d".format(h, m, s),
            fontFamily = FontFamily(Font(R.font.digital7_mono)),
            fontSize = 30.sp,
            color = color,
            modifier = Modifier.clickable { if (vm.timerStatus == TimerStatus.PAUSED) showSelector = true }
        )
        Row(modifier = Modifier.fillMaxWidth(0.5f), horizontalArrangement = Arrangement.SpaceBetween) {
            SmallIconButton(Icons.Default.KeyboardArrowUp, "Relax Time Up") { vm.incrementRelaxTime() }
            SmallIconButton(Icons.Default.KeyboardArrowDown, "Relax Time Down") { vm.decrementRelaxTime() }
        }
        if (showSelector) {
            TimePickerDialog(totalSeconds = vm.scheduledRelaxTimeInSeconds.collectAsState().value) { res ->
                vm.setRelaxTime(res)
                showSelector = false
            }
        }
    }
}

@Composable
private fun PlayPauseButton(vm: TimerViewModel, onMeasured: (Int) -> Unit) {
    val icon = if (vm.timerStatus == TimerStatus.PAUSED) {
        Icons.Default.PlayArrow
    } else {
        ImageVector.vectorResource(id = R.drawable.baseline_pause_24)
    }
    IconButton(
        onClick = { vm.togglePlayPause() },
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Blue)
            .onGloballyPositioned { onMeasured(it.size.height) }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (vm.timerStatus == TimerStatus.PAUSED) "Play" else "Pause",
            tint = Color.White,
        )
    }
}

@Composable
private fun SmallIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Blue)
            .fillMaxHeight()
    ) {
        Icon(imageVector = icon, contentDescription = description, tint = Color.White)
    }
}