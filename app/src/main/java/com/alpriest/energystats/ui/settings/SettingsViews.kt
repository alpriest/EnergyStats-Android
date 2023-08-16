package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpriest.energystats.R
import kotlinx.coroutines.launch

@Composable
fun SettingsTitleView(title: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.onSecondary,
        )
    }
}

@Composable
fun SettingsPage(content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
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
fun CancelSaveButtonView(navController: NavController, onSave: suspend () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Row {
        SettingsButton(stringResource(R.string.cancel), modifier = Modifier.weight(1.0f)) {
            navController.popBackStack()
        }

        Spacer(modifier = Modifier.width(12.dp))

        SettingsButton(stringResource(R.string.save), modifier = Modifier.weight(1.0f)) {
            coroutineScope.launch {
                onSave()
            }
        }
    }
}

@Composable
fun SettingsCheckbox(title: String, state: MutableState<Boolean>, onConfigUpdate: (Boolean) -> Unit, footer: AnnotatedString? = null) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    state.value = !state.value
                    onConfigUpdate(state.value)
                }
                .fillMaxWidth()
        ) {
            Text(
                title,
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.weight(1f)
            )

            Checkbox(
                checked = state.value,
                onCheckedChange = {
                    state.value = it
                    onConfigUpdate(it)
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
        }

        footer?.let {
            Text(
                it,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}