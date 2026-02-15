plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.statement_detect"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.statement_detect"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    val camerax_version = "1.3.1" // 或者 "1.4.0-alpha04" 等最新版本
    // 核心库 (必须)
    implementation("androidx.camera:camera-core:${camerax_version}")

    // ✨ 你的错误是因为缺了这一行！(Camera2 实现)
    implementation("androidx.camera:camera-camera2:${camerax_version}")

    // 生命周期库 (必须)
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")

    // 可选：如果你用了 CameraView (你的代码好像没用，但加上防备万一)
    implementation("androidx.camera:camera-view:${camerax_version}")
}