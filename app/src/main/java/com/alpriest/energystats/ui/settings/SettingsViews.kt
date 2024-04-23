package com.alpriest.energystats.ui.settings

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.dialog.AlertDialog

class SettingsPaddingValues {
    companion object {
        fun default(): PaddingValues {
            return PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 0.dp,
                bottom = 0.dp
            )
        }

        fun withVertical(): PaddingValues {
            return PaddingValues(
                start = 10.dp,
                end = 10.dp,
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
    footer: String? = null,
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
                        .padding(PaddingValues(top = 10.dp, start = 22.dp, end = 10.dp, bottom = 8.dp))
                        .fillMaxWidth()
                )
            }
        },
        footer = footer,
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
                .background(colors.surface)
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                content()

                footer?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.caption,
                        color = colors.onSecondary,
                        modifier = footerModifier.padding(bottom = 8.dp)
                    )
                }

                ErrorTextView(error)
            }
        }
    }
}

@Composable
fun ErrorTextView(text: String?) {
    text?.let {
        Text(
            it,
            style = MaterialTheme.typography.caption,
            color = colors.error,
            modifier = Modifier.padding(bottom = 8.dp)
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
            style = TextStyle.Default.copy(color = colors.onSecondary)
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
            .background(colors.background)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        content()
    }
}

@Composable
fun InfoButton(text: String) {
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    Image(
        imageVector = Icons.Default.Info,
        contentDescription = "Click for info",
        colorFilter = ColorFilter.tint(colors.primary),
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
fun SettingsCheckbox(title: String, infoText: String? = null, state: MutableState<Boolean>, onUpdate: (Boolean) -> Unit, footer: AnnotatedString? = null) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
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
                    color = colors.onSecondary
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
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
        }

        footer?.let {
            Text(
                it,
                style = MaterialTheme.typography.caption,
                color = colors.onSecondary,
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
                    color = colors.onSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            segmentedControl()
        }

        footer?.let {
            Text(
                it,
                style = MaterialTheme.typography.caption,
                color = colors.onSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

    }
}

@Composable
fun InlineSettingsNavButton(title: String, modifier: Modifier = Modifier, disclosureIcon: (() -> ImageVector)? = { Icons.Default.ChevronRight }, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .indication(interactionSource, rememberRipple()),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(all = 0.dp),
        border = null
    ) {
        Row(
            modifier = if (disclosureIcon == null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = colors.onSecondary,
                style = TextStyle.Default
            )

            disclosureIcon?.let {
                Icon(
                    imageVector = it(),
                    contentDescription = "Tap for more",
                    modifier = Modifier.padding(end = 12.dp),
                    tint = colors.onSecondary
                )
            }
        }
    }
}

@Composable
fun SettingsNavButton(title: String, modifier: Modifier = Modifier, disclosureIcon: (() -> ImageVector)? = { Icons.Default.ChevronRight }, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .indication(interactionSource, rememberRipple())
    ) {
        Row(
            modifier = if (disclosureIcon == null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = colors.onPrimary
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
