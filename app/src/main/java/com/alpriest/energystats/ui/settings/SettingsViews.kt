package com.alpriest.energystats.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R

@Composable
fun SettingsColumnWithChild(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(10.dp),
    content: @Composable () -> Unit
) {
    SettingsColumnWithChildAndFooter(modifier = modifier, content = content, footer = null, error = null, padding = padding)
}

@Composable
fun SettingsColumnWithChildAndFooter(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    footer: String?,
    error: String?,
    padding: PaddingValues = PaddingValues(10.dp)
) {
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
fun SettingsTitleView(title: String, extra: @Composable () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
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
fun SettingsButtonList(content: @Composable () -> Unit) {
    Column {
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