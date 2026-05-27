import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

// 将逻辑挪到 plugins 之后
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.afinal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.afinal"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 优先从 local.properties 读取，读不到再看 project 属性
        val plantNetKey = localProperties.getProperty("PLANTNET_API_KEY")
            ?: project.findProperty("PLANTNET_API_KEY")?.toString() ?: ""
        val trefleToken = localProperties.getProperty("TREFLE_API_TOKEN")
            ?: project.findProperty("TREFLE_API_TOKEN")?.toString() ?: ""
        val deepseekKey = localProperties.getProperty("DEEPSEEK_API_KEY")
            ?: project.findProperty("DEEPSEEK_API_KEY")?.toString() ?: ""

        buildConfigField("String", "PLANTNET_API_KEY", "\"$plantNetKey\"")
        buildConfigField("String", "TREFLE_API_TOKEN", "\"$trefleToken\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepseekKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Room 数据库
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Retrofit 网络请求
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // 协程测试
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    // 图片加载库
    implementation(libs.coil.compose)
    // 下拉刷新（Accompanist）
    implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")
}
