package com.alpriest.energystats.services

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.alpriest.energystats.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

@SuppressLint("ComposableNaming")
@Composable
fun trackScreenView(name: String, className: String) {
    if (BuildConfig.DEBUG) return

    val firebaseAnalytics = FirebaseAnalytics.getInstance(LocalContext.current)

    LaunchedEffect(Unit) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, name)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, className)
        }
    }
}
