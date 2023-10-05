package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpriest.energystats.R
import kotlinx.coroutines.launch

@Composable
fun ContentWithBottomButtons(navController: NavController, onSave: suspend () -> Unit, content: @Composable BoxScope.() -> Unit, modifier: Modifier = Modifier, footer: @Composable ColumnScope.() -> Unit = {}) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        content()

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Divider(
                    color = Color.LightGray, modifier = modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                CancelSaveButtonView(
                    navController,
                    onSave = onSave,
                    modifier = modifier
                        .background(colors.surface)
                        .padding(12.dp),
                    footer = footer
                )
            }
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
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}

@Composable
fun CancelSaveButtonView(navController: NavController, onSave: suspend () -> Unit, modifier: Modifier = Modifier, footer: @Composable ColumnScope.() -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(modifier = modifier) {
            SettingsNavButton(
                stringResource(R.string.cancel),
                modifier = Modifier.weight(1.0f),
                disclosureIcon = null
            ) {
                navController.popBackStack()
            }

            Spacer(modifier = Modifier.width(12.dp))

            SettingsNavButton(
                stringResource(R.string.save), modifier = Modifier.weight(1.0f),
                disclosureIcon = null
            ) {
                coroutineScope.launch {
                    onSave()
                }
            }
        }

        footer()
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
                modifier = Modifier.weight(1f)
            )

            Checkbox(
                checked = state.value,
                onCheckedChange = {
                    state.value = it
                    onUpdate(it)
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
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