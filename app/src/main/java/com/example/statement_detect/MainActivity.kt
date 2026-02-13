package com.example.statement_detect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.statement_detect.ui.theme.Statement_DetectTheme
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Statement_DetectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // ✅ 修改：不再传递 Activity 参数
                    RequestCameraPermission(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ✨ 新增：一个专门用来处理权限的 Wrapper 组件
@Composable
fun RequestCameraPermission(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 检查当前是否已经有权限
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 创建权限请求启动器
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
        // ✅ 只有有权限了，才显示相机界面
        StartCamera(modifier = modifier)
    } else {
        // 没有权限时显示的提示
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("需要相机权限才能拍照")
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("授予权限")
            }
        }
    }
}
@Composable
fun StartCamera(modifier: Modifier) {
    val context= LocalContext.current//当前上下文信息
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember{ mutableStateOf(null) }
    val preview = remember { androidx.camera.core.Preview.Builder().build() }
    val previewView = remember { mutableStateOf<PreviewView?>(null) }
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider=cameraProviderFuture.get()
        val imageCaptureUseCase = ImageCapture.Builder()
            // 设置捕获模式为最小延迟（拍照速度优先）
            // 另一个选项是 CAPTURE_MODE_MAXIMIZE_QUALITY（质量优先）
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // 设置闪光灯模式为自动
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build() // 构建 ImageCapture 对象
        imageCapture=imageCaptureUseCase
        val cameraSelector= CameraSelector.DEFAULT_FRONT_CAMERA//选择前置摄像头
        try {
            // 先解绑所有之前绑定的用例，避免冲突
            cameraProvider.unbindAll()

            // 将摄像头绑定到生命周期，并绑定预览和图像捕获两个用例
            cameraProvider.bindToLifecycle(
                lifecycleOwner,          // 生命周期所有者
                cameraSelector,          // 摄像头选择器（前置/后置）
                imageCaptureUseCase,      // 图像捕获用例
                preview
            )
        } catch (exc: Exception) {
            // 捕获异常并记录日志
            Log.e("CameraCapture", "摄像头绑定失败", exc)
        }
    }
    Box(){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ){
            var imgToShow: Bitmap? by remember {mutableStateOf(null)}
            var isAtWork: Boolean by remember {mutableStateOf(false)}

            Text(" APP")
            Button(onClick = {//测试拍照用按钮
                captureUserPhoto(
                    imageCapture=imageCapture,
                    context=context,){
                        bitmap -> imgToShow=bitmap
                }
            }) {
                Text("Photo(Test)")
            }
            if(imgToShow!=null){

                ShowReminderWithPhoto(image = imgToShow!!,){
                        res -> isAtWork=res
                    imgToShow=null
                }

            }
        }

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { view ->
                    // 当 PreviewView 创建时，将其赋值给 previewView
                    previewView.value = view
                    // 如果相机已经初始化，立即设置 surfaceProvider
                    preview.setSurfaceProvider(view.surfaceProvider)
                    }
                },
            modifier = Modifier.align(alignment = Alignment.TopEnd)
                            .padding(10.dp)// 根据需要调整尺寸
                .size(180.dp,240.dp)
        )
    }
}

fun captureUserPhoto(imageCapture: ImageCapture?,
                     context: Context,
                     onPhotoCaptured: (Bitmap) -> Unit
){
    val imageCaptureInstance= imageCapture?:return
    imageCaptureInstance.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "拍照失败: ${exception.message}", exception)
            }

            override fun onCaptureSuccess(image: ImageProxy) {
                // ✅ 步骤1完成：拍下当前图像
                val bitmap = image.toBitmap()

                Log.d("Camera", "成功拍摄用户照片: ${bitmap.width}x${bitmap.height}")

                // TODO: 步骤2 - 图像处理
                onPhotoCaptured(bitmap)
                // 必须关闭 ImageProxy
                image.close()
            }
        }
    )
}
@Composable
fun ShowReminderWithPhoto(image: Bitmap,onDismiss:(Boolean)->Unit){

    Dialog(onDismissRequest = {
        onDismiss(false)//这里可能存在问题
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        )
    ) {
        Column(
            modifier= Modifier.padding(10.dp),
            horizontalAlignment= Alignment.CenterHorizontally
        ) {
            val imageBitmap: ImageBitmap = image.asImageBitmap()
            Image(bitmap = imageBitmap , contentDescription = "captured")
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Statement_DetectTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            StartCamera(
                modifier = Modifier.padding(innerPadding))
        }
    }
}