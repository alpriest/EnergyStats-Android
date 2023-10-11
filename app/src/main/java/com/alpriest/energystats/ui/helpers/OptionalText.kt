package com.alpriest.energystats.ui.helpers

import androidx.compose.runtime.Composable

@Composable
fun <T: Any> OptionalView(optional: T?, closure: @Composable (T) -> Unit) {
    optional?.let {
        closure(it)
    }
}