package com.example.statement_detect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.statement_detect.ui.theme.Statement_DetectTheme
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import com.example.statement_detect.ui.theme.DigitalMono

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Statement_DetectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // ✅ 修改：不再传递 Activity 参数
                    ClockGUI(modifier = Modifier.padding(innerPadding))
                    RequestCameraPermission(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ClockGUI(modifier: Modifier = Modifier) {
    var paused: Boolean by remember { mutableStateOf(true) }
    var work_h_time: Int by remember { mutableStateOf(0) }
    var work_m_time: Int by remember { mutableStateOf(0) }
    var work_s_time: Int by remember { mutableStateOf(0) }
    var relax_h_time: Int by remember { mutableStateOf(0) }
    var relax_m_time: Int by remember { mutableStateOf(0) }
    var relax_s_time: Int by remember { mutableStateOf(0) }
    var round: Int by remember { mutableStateOf(0) }
    var roundsHeight by remember { mutableStateOf(0) }
    var playButtonHeight by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {//最外层框架,罩住整个画面
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(

                           Color(0xFF000000),
                            Color(0xFF000000),
                            Color(0xFF022150),
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 6.dp,
                    color = Color(0xFF6D7C8A),
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
                        color = Color(0xFFFFFFFF),
                    )
                    Text(
                        text = "%01d".format(round),
                        fontFamily = FontFamily(Font(R.font.digital7_mono)),
                        fontSize = 30.sp,
                        color = Color(0xFF57EC5E)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                round++
                                round %= 10
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Blue)
                                .fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "向上",
                                tint = Color.White,
                            )
                        }

                        IconButton(
                            onClick = {
                                round--
                                round = (round + 10) % 10
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Blue)
                                .fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "向上",
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
                            color = Color(0xFFFFFFFF),
                        )
                        Text(
                            text = "%02d:%02d:%02d".format(work_h_time, work_m_time, work_s_time),
                            fontFamily = FontFamily(Font(R.font.digital7_mono)),
                            fontSize = 30.sp,
                            color = Color(0xFF57EC5E)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    work_s_time++
                                    work_m_time += work_s_time / 60
                                    work_s_time %= 60
                                    work_h_time += work_m_time / 60
                                    work_m_time %= 60
                                    work_h_time %= 24
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "向上",
                                    tint = Color.White,
                                )
                            }

                            IconButton(
                                onClick = {
                                    work_s_time--
                                    work_m_time += (work_s_time - 59) / 60
                                    work_s_time = (work_s_time + 60) % 60
                                    work_h_time += (work_m_time - 59) / 60
                                    work_m_time = (work_m_time + 60) % 60
                                    work_h_time = (work_h_time + 24) % 24
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "向上",
                                    tint = Color.White,
                                )
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
                            color = Color(0xFFFFFFFF),
                        )
                        Text(
                            text = "%02d:%02d:%02d".format(relax_h_time, relax_m_time, relax_s_time),
                            fontFamily = FontFamily(Font(R.font.digital7_mono)),
                            fontSize = 30.sp,
                            color = Color(0xFF57EC5E)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    relax_s_time++
                                    relax_m_time += relax_s_time / 60
                                    relax_s_time %= 60
                                    relax_h_time += relax_m_time / 60
                                    relax_m_time %= 60
                                    relax_h_time %= 24
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "向上",
                                    tint = Color.White,
                                )
                            }

                            IconButton(
                                onClick = {
                                    relax_s_time--
                                    relax_m_time += (relax_s_time - 59) / 60
                                    relax_s_time = (relax_s_time + 60) % 60
                                    relax_h_time += (relax_m_time - 59) / 60
                                    relax_m_time = (relax_m_time + 60) % 60
                                    relax_h_time = (relax_h_time + 24) % 24
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "向上",
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }

                // 添加底部间距以实现垂直居中
                Spacer(modifier = Modifier.height(with(LocalDensity.current) {
                    (roundsHeight - playButtonHeight).toDp()
                }))

                IconButton(
                    onClick = {
                        paused = !paused
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Blue)
                        .onGloballyPositioned { coordinates ->
                            playButtonHeight = coordinates.size.height
                        }
                ) {
                    var Icon_to_show: ImageVector
                    if (paused) {
                        Icon_to_show = Icons.Default.PlayArrow
                    } else {
                        Icon_to_show = ImageVector.vectorResource(id = R.drawable.baseline_pause_24)
                    }
                    Icon(
                        imageVector = Icon_to_show,
                        contentDescription = "向上",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
// 新增：一个专门用来处理权限的 Wrapper 组件
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

            /*Text(" APP")
            Button(onClick = {//测试拍照用按钮
                captureUserPhoto(
                    imageCapture=imageCapture,
                    context=context,){
                        bitmap -> imgToShow=bitmap
                }
            }) {
                Text("Photo(Test)")
            }*/
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
                    // 关键：使用 FILL_CENTER 裁剪并填满视图
                    view.scaleType = PreviewView.ScaleType.FILL_CENTER
                    // 可选：设置背景透明，让裁剪区域外的部分显示下层内容
                    view.background = null
                    // 使用 COMPATIBLE 模式提高兼容性
                    view.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView.value = view
                    preview.setSurfaceProvider(view.surfaceProvider)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                // 使用 requiredSize 强制指定大小，避免父布局约束干扰
                .requiredSize(180.dp, 240.dp)  // 根据需要调整
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
            ClockGUI(modifier = Modifier.padding(innerPadding))
            StartCamera(
                modifier = Modifier.padding(innerPadding))
        }
    }
}