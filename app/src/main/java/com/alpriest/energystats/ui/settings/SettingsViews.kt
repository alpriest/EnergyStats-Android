package com.alpriest.energystats.ui.settings

import android.content.Context
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R

@Composable
fun SettingsColumn(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    error: String? = null,
    padding: PaddingValues = PaddingValues(10.dp),
    content: @Composable () -> Unit,
) {
    SettingsColumnWithChild(
        modifier = modifier,
        header = {
            header?.let {
                SettingsTitleView(
                    it,
                    modifier = Modifier.padding(PaddingValues(top = 10.dp, start = 22.dp, end = 10.dp, bottom = 8.dp))
                )
            }
        },
        footer = footer,
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
    error: String? = null,
    padding: PaddingValues = PaddingValues(10.dp),
    content: @Composable () -> Unit,
) {
    Column {
        header?.let { it() }

        Column(
            modifier = modifier
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
                        modifier = Modifier.padding(bottom = 8.dp)
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
            style = MaterialTheme.typography.h4,
            color = colors.onSecondary,
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
fun SettingsCheckbox(title: String, state: MutableState<Boolean>, onUpdate: (Boolean) -> Unit, footer: AnnotatedString? = null) {
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
            Text(
                title,
                color = colors.onSecondary,
                modifier = Modifier.weight(1f),
                style = TextStyle.Default.copy(fontWeight = FontWeight.SemiBold)
            )

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
                    modifier = Modifier.weight(1f),
                    style = TextStyle.Default.copy(fontWeight = FontWeight.Bold)
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
                style = TextStyle.Default.copy(fontWeight = FontWeight.SemiBold)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Tap for more",
                modifier = Modifier.padding(end = 12.dp)
            )
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