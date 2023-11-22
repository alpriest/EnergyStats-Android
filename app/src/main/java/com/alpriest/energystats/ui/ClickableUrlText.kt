package com.alpriest.energystats.ui

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

fun makeUrlAnnotatedString(text: String): AnnotatedString {
    val clickableText = "https://toolkit.solcast.com.au"
    val urlPattern = "(https?:\\/\\/[^ ]+)".toRegex()

    val annotatedString = buildAnnotatedString {
        append(text)

        urlPattern.findAll(text).forEach { matchResult ->
            val url = matchResult.value
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            if (start >= 0) {
                addStringAnnotation(
                    tag = "URL",
                    annotation = clickableText,
                    start = start,
                    end = end
                )
                addStyle(
                    SpanStyle().copy(color = Color.Blue, textDecoration = TextDecoration.Underline),
                    start = start,
                    end = end
                )
            }
        }
    }

    return annotatedString
}

@Composable
fun ClickableUrlText(text: String, modifier: Modifier = Modifier) {
    val annotatedString = makeUrlAnnotatedString(text)
    val context = LocalUriHandler.current

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations("URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    context.openUri(annotation.item)
                }
        },
        modifier = modifier
    )
}