package com.example.statement_detect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.statement_detect.camera.RequestCameraPermission
import com.example.statement_detect.timer.TimerViewModel
import com.example.statement_detect.timer.TimerViewModelFactory
import com.example.statement_detect.ui.AppDrawer
import com.example.statement_detect.ui.AppNavigation
import com.example.statement_detect.ui.AppScaffold
import com.example.statement_detect.ui.ClockGUI
import com.example.statement_detect.ui.theme.Statement_DetectTheme
import com.example.statement_detect.ui.AppNavigation
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Statement_DetectTheme {
                val context = LocalContext.current
                val vm: TimerViewModel = viewModel(
                    factory = TimerViewModelFactory(context = context)
                )
                AppNavigation(vm = vm)
            }
        }
    }
}