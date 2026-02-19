package com.example.statement_detect.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import java.util.concurrent.Executors

val cameraExecutor = Executors.newSingleThreadExecutor()

fun rotateBitmap(bitmap: Bitmap, rotationAngle: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(rotationAngle) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    val newWidth: Int
    val newHeight: Int
    if (originalWidth > originalHeight) {
        newWidth = maxDimension
        newHeight = (maxDimension * originalHeight / originalWidth)
    } else {
        newHeight = maxDimension
        newWidth = (maxDimension * originalWidth / originalHeight)
    }
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun captureUserPhoto(
    imageCapture: ImageCapture?,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val imageCaptureInstance = imageCapture ?: return

    imageCaptureInstance.takePicture(
        cameraExecutor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "拍照失败: ${exception.message}", exception)
            }

            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val originalBitmap = image.toBitmap()
                    val scaledBitmap = scaleBitmap(originalBitmap, 640)
                    // 修复：使用 rotatedBitmap 而不是 scaledBitmap
                    val rotatedBitmap = rotateBitmap(scaledBitmap, image.imageInfo.rotationDegrees.toFloat())
                    Log.d("Camera", "成功拍摄并压缩: ${rotatedBitmap.width}x${rotatedBitmap.height}")
                    Handler(Looper.getMainLooper()).post {
                        onPhotoCaptured(rotatedBitmap)
                    }
                } catch (e: Exception) {
                    Log.e("Camera", "图片处理出错: ${e.message}")
                } finally {
                    image.close()
                }
            }
        }
    )
}