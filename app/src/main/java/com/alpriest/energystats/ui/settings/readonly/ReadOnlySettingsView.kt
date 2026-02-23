package com.alpriest.energystats.ui.settings.readonly

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadOnlySettingsView(
    configManager: ConfigManaging,
    viewModel: ReadOnlySettingsViewModel = viewModel(factory = ReadOnlySettingsViewModelFactory(configManager))
) {
    val viewData = viewModel.viewDataStream.collectAsStateWithLifecycle().value
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    MonitorAlertDialog(viewModel)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AnimatedContent(
            targetState = viewData.isReadOnly,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally(animationSpec = tween(250)) { fullWidth -> -fullWidth } + fadeIn(
                        animationSpec = tween(250)
                    ) togetherWith slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth } + fadeOut(
                        animationSpec = tween(250)
                    )
                } else {
                    slideInHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth } + fadeIn(
                        animationSpec = tween(250)
                    ) togetherWith slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> -fullWidth } + fadeOut(
                        animationSpec = tween(250)
                    )
                }
            },
            label = "readOnlyStatus"
        ) { readOnly ->
            Text(
                text = if (readOnly) {
                    "Inverter and battery changes are prevented."
                } else {
                    "Inverter and battery changes are permitted."
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewData.isReadOnly) {
                Text(
                    text = "Enter current passcode",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Enter your 4-digit passcode to turn off read-only mode.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Choose a passcode",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Enter a 4-digit passcode to turn on read-only mode.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            PasscodeInput(
                passcode = viewData.passcode,
                onPasscodeChanged = { viewModel.onPasscodeChanged(it) },
                focusRequester = focusRequester,
                onDone = { focusManager.clearFocus() }
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "If you forget your passcode, you can log out and in again.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReadOnlySettingsViewPreviewOnly() {
    ReadOnlySettingsView(
        FakeConfigManager(),
        ReadOnlySettingsViewModel(FakeConfigManager())
    )
}