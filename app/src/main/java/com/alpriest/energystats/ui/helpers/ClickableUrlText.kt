package com.alpriest.energystats.ui.helpers

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.isDarkMode
import com.alpriest.energystats.shared.ui.WebLinkColorInDarkTheme
import com.alpriest.energystats.shared.ui.WebLinkColorInLightTheme
import kotlinx.coroutines.flow.StateFlow

fun makeUrlAnnotatedString(text: String, linkColor: Color): AnnotatedString {
    val urlPattern = "(https?://[^ ]+)|(\\S+@\\S+)".toRegex()
    val emailPattern = "(\\S+@\\S+)".toRegex()

    val annotatedString = buildAnnotatedString {
        append(text)

        urlPattern.findAll(text).forEach { matchResult ->
            val url: String = if (emailPattern.matches(matchResult.value)) {
                "mailto:$matchResult.value"
            } else {
                matchResult.value
            }
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            if (start >= 0) {
                addStringAnnotation(
                    tag = "URL",
                    annotation = url,
                    start = start,
                    end = end
                )
                addStyle(
                    SpanStyle().copy(color = linkColor, textDecoration = TextDecoration.Underline),
                    start = start,
                    end = end
                )
            }
        }
    }

    return annotatedString
}

@Composable
fun ClickableUrlText(text: String, modifier: Modifier = Modifier, textStyle: TextStyle, appSettingsStream: StateFlow<AppSettings>) {
    val annotatedString = makeUrlAnnotatedString(text, linkColor = webLinkColor(isDarkMode(appSettingsStream)))
    val context = LocalUriHandler.current

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations("URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    context.openUri(annotation.item)
                }
        },
        style = textStyle,
        modifier = modifier
    )
}

@Composable
fun webLinkColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        WebLinkColorInDarkTheme
    } else {
        WebLinkColorInLightTheme
    }
}
