package com.example.statement_detect.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.statement_detect.ui.ShowReminderWithPhoto

@Composable
fun RequestCameraPermission(
    modifier: Modifier = Modifier,
    shouldTakePhoto: Boolean = false,
    onCaptureTriggered: () -> Unit = {}
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )
    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasPermission) {
        StartCamera(modifier = modifier, shouldTakePhoto = shouldTakePhoto, onCaptureTriggered = onCaptureTriggered)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("需要相机权限才能拍照")
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("授予权限") }
        }
    }
}

@Composable
fun StartCamera(
    modifier: Modifier,
    shouldTakePhoto: Boolean = false,
    onCaptureTriggered: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val preview = remember { androidx.camera.core.Preview.Builder().build() }
    val imgToShow = remember { mutableStateListOf<Bitmap>() }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val imageCaptureUseCase = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        imageCapture = imageCaptureUseCase
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                imageCaptureUseCase,
                preview
            )
        } catch (exc: Exception) {
            Log.e("CameraCapture", "摄像头绑定失败", exc)
        }
    }

    LaunchedEffect(shouldTakePhoto) {
        if (shouldTakePhoto && imageCapture != null) {
            onCaptureTriggered()
            captureUserPhoto(imageCapture) { bitmap ->
                imgToShow.add(bitmap)
            }
        }
    }

    Box {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (imgToShow.isNotEmpty()) {
                ShowReminderWithPhoto(image = imgToShow.first()) {
                    if (imgToShow.isNotEmpty()) imgToShow.removeAt(0)
                }
            }
        }

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    background = null
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    preview.setSurfaceProvider(this.surfaceProvider)
                }
            },
            modifier = modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .requiredSize(150.dp, 200.dp)
        )
    }
}