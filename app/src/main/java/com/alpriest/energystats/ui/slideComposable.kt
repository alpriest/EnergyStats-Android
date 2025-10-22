package com.alpriest.energystats.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.slideComposable(
    route: String,
    durationMillis: Int = 250,
    forward: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Left,
    back: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Right,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route = route,
        enterTransition = {
            slideIntoContainer(forward, animationSpec = tween(durationMillis, easing = EaseIn))
        },
        exitTransition = {
            slideOutOfContainer(forward, animationSpec = tween(durationMillis, easing = EaseOut))
        },
        popEnterTransition = {
            slideIntoContainer(back, animationSpec = tween(durationMillis, easing = EaseIn))
        },
        popExitTransition = {
            slideOutOfContainer(back, animationSpec = tween(durationMillis, easing = EaseOut))
        },
        content = content
    )
}