import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.firebase.firebase-perf") version "2.0.2" apply false
}

val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    namespace = "com.alpriest.energystats"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.alpriest.energystats"
        minSdk = 26
        targetSdk = 36
        versionCode = providers.gradleProperty("VERSION_CODE").get().toInt()
        versionName = providers.gradleProperty("VERSION_NAME").get()

        buildConfigField(type = "String", name = "GOOGLE_MAPS_APIKEY", value = localProperties.getProperty("GOOGLE_MAPS_APIKEY"))
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation)
    implementation(libs.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.navigation)
    implementation(libs.compose.coil)
    implementation(libs.compose.markdown)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.shimmer)
    implementation(libs.vico)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.runtimeKtx)
    implementation(libs.lifecycle.viewmodelCompose)
    implementation(libs.androidx.material3)
    implementation(libs.gms.play.services.wearable)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.chucker)
    implementation(libs.rollingnumbers)
    implementation(libs.firebase.perf)
    implementation(project(":shared"))
    wearApp(project(":wear"))
}