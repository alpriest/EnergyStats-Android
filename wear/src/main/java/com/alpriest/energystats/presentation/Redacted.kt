package com.alpriest.energystats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.rememberPlaceholderState
import com.alpriest.energystats.shared.helpers.asPercent
import com.alpriest.energystats.shared.helpers.kW
import com.alpriest.energystats.shared.helpers.kWh

@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun TextWithPlaceholder(text: String?, textStyle: TextStyle) {
    val placeholderState = rememberPlaceholderState { text != null }

    Text(
        text = buildAnnotatedString {
            (text ?: "123").forEach { char ->
                if (char == '.' || char == ',') {
                    withStyle(SpanStyle(letterSpacing = (-2).sp)) {
                        append(char)
                    }
                } else {
                    append(char)
                }
            }
        },
        modifier = Modifier.placeholder(placeholderState),
        style = textStyle
    )
}

@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun kWWithPlaceholder(amount: Double?, textStyle: TextStyle) {
    val placeholderState = rememberPlaceholderState { amount != null }
    val text = (amount ?: 8.8).kW(2)

    TextWithPlaceholder(text, textStyle)
}

@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun kWhWithPlaceholder(amount: Double?, textStyle: TextStyle) {
    val placeholderState = rememberPlaceholderState { amount != null }
    val text = (amount ?: 8.8).kWh(2)

    TextWithPlaceholder(text, textStyle)
}

@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun RedactedPercentage(amount: Double?, textStyle: TextStyle) {
    val placeholderState = rememberPlaceholderState { amount != null }

    Text(
        text = (amount ?: 8.8).asPercent(),
        modifier = Modifier.placeholder(placeholderState),
        style = textStyle
    )
}