package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ContentWithBottomButtonPair(
    navController: NavController,
    onConfirm: suspend () -> Unit,
    content: @Composable BoxScope.(modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
    labels: ButtonLabels = ButtonLabels.Defaults(LocalContext.current)
) {
    val buttons = listOf(
        BottomButtonConfiguration(title = labels.left, onTap = { navController.popBackStack() }),
        BottomButtonConfiguration(title = labels.right, onTap = onConfirm),
    )

    ContentWithBottomButtons(content, modifier, footer, buttons)
}

@Composable
fun ContentWithBottomButtons(
    content: @Composable BoxScope.(modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
    buttons: List<BottomButtonConfiguration>
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        content(Modifier.padding(bottom = 86.dp))

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                HorizontalDivider(
                    color = Color.LightGray,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )

                BottomButtonsView(
                    modifier = Modifier
                            .background(colorScheme.surface)
                            .padding(12.dp),
                    footer = footer,
                    buttons = buttons
                )
            }
        }
    }
}

data class BottomButtonConfiguration(
    val title: String,
    val onTap: (suspend () -> Unit)
)

@Composable
fun BottomButtonsView(
    modifier: Modifier = Modifier,
    footer: @Composable (ColumnScope.() -> Unit) = {},
    buttons: List<BottomButtonConfiguration>
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(modifier = modifier) {
            buttons.forEachIndexed { index, button ->
                SettingsNavButton(
                    button.title,
                    modifier = Modifier.weight(1.0f),
                    disclosureIcon = null
                ) {
                    scope.launch {
                        button.onTap()
                    }
                }

                if (index + 1 < buttons.count()) {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        footer()
    }
}