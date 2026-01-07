import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}
android {
    namespace = "com.alpriest.energystats"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        val api = 36
        applicationId = "com.alpriest.energystats"
        minSdk = 28
        targetSdk = api
        val device = 1 // wear
        val base = providers.gradleProperty("VERSION_CODE_BASE").get().toInt()   // e.g. 2164
        val patch = providers.gradleProperty("VERSION_CODE_PATCH").get().toInt() // 0..99

        require(patch in 0..99) { "VERSION_CODE_PATCH must be 0..99" }
        require(base in 0..9999) { "VERSION_CODE_BASE must be 0..9999" }

        versionCode = (api * 10_000_000) + (device * 1_000_000) + (base * 100) + patch
        versionName = providers.gradleProperty("VERSION_NAME").get()
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.compose.activity)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.tiles)
    implementation(libs.androidx.tiles.material)
    implementation(libs.androidx.tiles.tooling.preview)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.androidx.watchface.complications.data.source.ktx)
    implementation(libs.lifecycle.process)
    implementation(libs.androidx.wear)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.tiles.tooling)
    implementation(project(":shared"))
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.lifecycle.viewmodelCompose)
    implementation(libs.gson)
    implementation(libs.lifecycle.runtimeKtx)
}