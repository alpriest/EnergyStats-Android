package com.alpriest.energystats.services

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

@SuppressLint("ComposableNaming")
@Composable
fun trackScreenView(name: String, className: String) {
    val firebaseAnalytics = FirebaseAnalytics.getInstance(LocalContext.current)

    LaunchedEffect(Unit) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, name)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, className)
        }
    }
}
