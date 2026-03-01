package com.example.statement_detect.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.statement_detect.timer.TimerViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun SettingsGUI(vm: TimerViewModel, modifier: Modifier) {
    val focusManager = LocalFocusManager.current
    val segments by vm.segments.collectAsState()
    Column(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        }.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepperField(
            modifier = modifier,
            label = "Segments",
            value = segments,
            onValueChange = { vm.setSegments(it) },
            min = 1,
            max = 10
        )
    }
}
@Composable
fun StepperField(
    modifier: Modifier,
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
    max: Int = 10
) {
    var textState by remember(value) { mutableStateOf(value.toString()) }

    fun clamp(v: Int) = v.coerceIn(min, max)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label : ",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { onValueChange(clamp(value + 1)) }) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = textState,
            onValueChange = {
                textState = it
                it.toIntOrNull()?.let { num -> onValueChange(clamp(num)) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        IconButton(onClick = { onValueChange(clamp(value - 1)) }) {
            Icon(Icons.Default.Remove, contentDescription = null)
        }
    }
}