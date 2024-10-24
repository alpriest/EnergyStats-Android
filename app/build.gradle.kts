import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    namespace = "com.alpriest.energystats"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alpriest.energystats"
        minSdk = 26
        targetSdk = 34
        versionCode = 218
        versionName = "2.72"

        buildConfigField(type = "String", name = "GOOGLE_MAPS_APIKEY", value = localProperties.getProperty("GOOGLE_MAPS_APIKEY"))
    }
    buildFeatures {
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation)
    implementation(libs.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.preview)
    implementation(libs.compose.navigation)
    implementation(libs.compose.coil)
    implementation(libs.compose.markdown)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.shimmer)
    implementation(libs.vico)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.runtimeKtx)
    implementation(libs.lifecycle.viewmodelCompose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}