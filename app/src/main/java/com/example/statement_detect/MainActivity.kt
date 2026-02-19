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
import com.example.statement_detect.camera.RequestCameraPermission
import com.example.statement_detect.ui.ClockGUI
import com.example.statement_detect.ui.theme.Statement_DetectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Statement_DetectTheme {
                var needPhoto by remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ClockGUI(modifier = Modifier.padding(innerPadding)) {
                        needPhoto = true
                    }
                    RequestCameraPermission(
                        modifier = Modifier.padding(innerPadding),
                        shouldTakePhoto = needPhoto,
                        onCaptureTriggered = { needPhoto = false }
                    )
                }
            }
        }
    }
}