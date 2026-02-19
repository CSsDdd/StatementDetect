package com.example.statement_detect.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.statement_detect.R
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    totalSeconds: Int,
    onDismiss: (Int) -> Unit
) {
    val bufferCount = 3
    val hourItems = remember { List(bufferCount) { null } + (0..23).toList() + List(bufferCount) { null } }
    val minuteItems = remember { List(bufferCount) { null } + (0..59).toList() + List(bufferCount) { null } }
    val secondItems = remember { List(bufferCount) { null } + (0..59).toList() + List(bufferCount) { null } }

    val targetHourIndex = totalSeconds / 3600
    val targetMinuteIndex = (totalSeconds / 60) % 60
    val targetSecondIndex = totalSeconds % 60

    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = targetHourIndex)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = targetMinuteIndex)
    val secondState = rememberLazyListState(initialFirstVisibleItemIndex = targetSecondIndex)

    val density = LocalDensity.current
    val columnHeight = 200.dp
    val itemHeight = 48.dp
    val targetOffsetPx = with(density) { ((columnHeight - itemHeight) / 2).roundToPx() }

    LaunchedEffect(Unit) {
        launch { hourState.scrollToItem(targetHourIndex, targetOffsetPx) }
        launch { minuteState.scrollToItem(targetMinuteIndex, targetOffsetPx) }
        launch { secondState.scrollToItem(targetSecondIndex, targetOffsetPx) }
    }

    Dialog(
        onDismissRequest = {
            val h = getCenterItemIndex(hourState, bufferCount, 0..23)
            val m = getCenterItemIndex(minuteState, bufferCount, 0..59)
            val s = getCenterItemIndex(secondState, bufferCount, 0..59)
            onDismiss(h * 3600 + m * 60 + s)
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.size(240.dp).clip(RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPickerColumn(items = hourItems, state = hourState, modifier = Modifier.weight(1f))
                Text(":", modifier = Modifier.padding(horizontal = 4.dp), color = Color.Black, fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.digital7_mono)))
                WheelPickerColumn(items = minuteItems, state = minuteState, modifier = Modifier.weight(1f))
                Text(":", modifier = Modifier.padding(horizontal = 4.dp), color = Color.Black, fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.digital7_mono)))
                WheelPickerColumn(items = secondItems, state = secondState, modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun getCenterItemIndex(state: LazyListState, bufferCount: Int, validRange: IntRange): Int {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return validRange.first
    val viewportCenter = layoutInfo.viewportEndOffset / 2f
    val closestItem = visibleItems.minByOrNull { item ->
        val itemCenter = item.offset + item.size / 2f
        kotlin.math.abs(itemCenter - viewportCenter)
    } ?: return validRange.first
    return (closestItem.index - bufferCount).coerceIn(validRange.first, validRange.last)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPickerColumn(items: List<Int?>, state: LazyListState, modifier: Modifier = Modifier) {
    val itemHeight = 48.dp
    val columnHeight = 200.dp
    LazyColumn(
        state = state,
        flingBehavior = rememberSnapFlingBehavior(state, SnapPosition.Center),
        modifier = modifier.height(columnHeight).clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.2f)).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items) { value ->
            Box(
                modifier = Modifier.height(itemHeight).fillMaxWidth().padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (value != null) {
                    Text(text = "%02d".format(value), fontFamily = FontFamily(Font(R.font.digital7_mono)),
                        color = Color.Black, fontSize = 20.sp)
                } else {
                    Spacer(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}