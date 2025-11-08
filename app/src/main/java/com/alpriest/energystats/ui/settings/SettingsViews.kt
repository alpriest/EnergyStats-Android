package com.alpriest.energystats.ui.settings

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.dialog.AlertDialog
import com.alpriest.energystats.ui.helpers.darkenColor
import com.alpriest.energystats.ui.theme.ESButton

object SettingsPadding {
    val COLUMN_BOTTOM: Dp = 12.dp
    val PANEL_OUTER_HORIZONTAL = 12.dp
    val PANEL_INNER_HORIZONTAL = 12.dp
    val CONTENT_BOTTOM = 24.dp
}

class SettingsPaddingValues {
    companion object {
        fun default(): PaddingValues {
            return PaddingValues(
                start = 0.dp,
                end = 0.dp,
                top = 0.dp,
                bottom = 0.dp
            )
        }

        fun withVertical(): PaddingValues {
            return PaddingValues(
                start = 0.dp,
                end = 0.dp,
                top = 10.dp,
                bottom = 10.dp
            )
        }
    }
}

@Composable
fun SettingsColumn(
    modifier: Modifier = Modifier,
    header: String? = null,
    headerExtra: @Composable () -> Unit = {},
    footer: String? = null,
    footerAnnotatedString: AnnotatedString? = null,
    footerModifier: Modifier = Modifier,
    error: String? = null,
    padding: PaddingValues = SettingsPaddingValues.default(),
    content: @Composable () -> Unit,
) {
    SettingsColumnWithChild(
        modifier = modifier,
        header = {
            header?.let {
                SettingsTitleView(
                    it,
                    modifier = Modifier
                        .padding(
                            PaddingValues(
                                top = 10.dp,
                                start = padding.calculateLeftPadding(LayoutDirection.Ltr) + SettingsPadding.PANEL_OUTER_HORIZONTAL,
                                end = padding.calculateRightPadding(LayoutDirection.Ltr) + SettingsPadding.PANEL_OUTER_HORIZONTAL,
                                bottom = 8.dp
                            )
                        )
                        .fillMaxWidth(),
                    headerExtra
                )
            }
        },
        footer = footer,
        footerAnnotatedString = footerAnnotatedString,
        footerModifier = footerModifier,
        error = error,
        padding = padding,
        content = content
    )
}

@Composable
fun SettingsColumnWithChild(
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    footer: String? = null,
    footerAnnotatedString: AnnotatedString? = null,
    footerModifier: Modifier = Modifier,
    error: String? = null,
    padding: PaddingValues = SettingsPaddingValues.default(),
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        header?.let { it() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface)
                .padding(padding)
        ) {
            Column(Modifier.padding(horizontal = SettingsPadding.PANEL_INNER_HORIZONTAL)) {
                content()

                ErrorTextView(error)
            }
        }

        footer?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                    Text(
                        it,
                        style = typography.bodySmall,
                        color = colorScheme.onSecondary.copy(alpha = 0.7f),
                        modifier = footerModifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        footerAnnotatedString?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                    Text(
                        it,
                        style = typography.bodySmall,
                        color = colorScheme.onSecondary.copy(alpha = 0.7f),
                        modifier = footerModifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorTextView(text: String?) {
    text?.let {
        Text(
            it,
            style = typography.bodyMedium,
            color = colorScheme.error,
            modifier = Modifier.padding(8.dp)
        )
    }
}

data class ButtonLabels(val left: String, val right: String) {
    companion object {
        fun Defaults(context: Context): ButtonLabels {
            return ButtonLabels(
                context.getString(R.string.cancel),
                context.getString(R.string.save)
            )
        }
    }
}

@Composable
fun SettingsTitleView(title: String, modifier: Modifier = Modifier, extra: @Composable () -> Unit = {}) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = TextStyle.Default.copy(color = colorScheme.onSecondary, fontWeight = Bold),
        )

        extra()
    }
}

@Composable
fun SettingsPage(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .darkenedBackground()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        content()
    }
}

@Composable
fun Modifier.darkenedBackground(): Modifier = this.then(
    Modifier.background(darkenedBackgroundColor())
)

@Composable
fun darkenedBackgroundColor(): Color {
    return darkenColor(colorScheme.background, 0.04f)
}

@Composable
fun InfoButton(text: String) {
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    Image(
        imageVector = Icons.Default.Info,
        contentDescription = "Click for info",
        colorFilter = ColorFilter.tint(colorScheme.primary),
        modifier = Modifier
            .padding(start = 4.dp)
            .clickable {
                message = text
            }
    )

    message?.let {
        AlertDialog(message = text, onDismiss = {
            message = null
        })
    }
}

@Composable
fun SettingsCheckbox(
    title: String,
    infoText: String? = null,
    state: MutableState<Boolean>,
    enabled: Boolean = true,
    onUpdate: (Boolean) -> Unit,
    footer: AnnotatedString? = null
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled) {
                    state.value = !state.value
                    onUpdate(state.value)
                }
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    color = colorScheme.onSecondary.copy(alpha = if (enabled) 1.0f else 0.5f)
                )

                infoText?.let {
                    InfoButton(it)
                }
            }

            Checkbox(
                checked = state.value,
                onCheckedChange = {
                    state.value = it
                    onUpdate(it)
                },
                enabled = enabled,
                colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary.copy(alpha = if (enabled) 1.0f else 0.5f))
            )
        }

        footer?.let {
            Text(
                it,
                style = typography.bodySmall,
                color = colorScheme.onSecondary.copy(alpha = if (enabled) 1.0f else 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun SettingsSegmentedControl(title: String? = null, segmentedControl: @Composable () -> Unit, footer: AnnotatedString? = null) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            title?.let {
                Text(
                    it,
                    color = colorScheme.onSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            segmentedControl()
        }

        footer?.let {
            Text(
                it,
                style = typography.bodySmall,
                color = colorScheme.onSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

    }
}

@Composable
fun InlineSettingsNavButton(
    title: String,
    modifier: Modifier = Modifier,
    disclosureIcon: (() -> ImageVector)? = { Icons.Default.ChevronRight },
    disclosureView: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(all = 0.dp),
        border = null
    ) {
        Row(
            modifier = if (disclosureIcon == null && disclosureView == null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = colorScheme.onSecondary,
                style = typography.bodyLarge
            )

            disclosureIcon?.let {
                Icon(
                    imageVector = it(),
                    contentDescription = "Tap for more",
                    modifier = Modifier.padding(end = 12.dp),
                    tint = colorScheme.onSecondary
                )
            }

            disclosureView?.let { it() }
        }
    }
}

@Composable
fun SettingsNavButton(title: String, modifier: Modifier = Modifier, disclosureIcon: (() -> ImageVector)? = { Icons.Default.ChevronRight }, onClick: () -> Unit) {
    ESButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
    ) {
        Row(
            modifier = if (disclosureIcon == null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = colorScheme.onPrimary
            )

            disclosureIcon?.let {
                Icon(
                    imageVector = it(),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SettingsBottomSpace() {
    Spacer(Modifier.height(SettingsPadding.CONTENT_BOTTOM))
}

@Composable
fun SlimButton(enabled: Boolean = true, onClick: () -> Unit, content: @Composable () -> Unit) {
    ESButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        content()
    }
}