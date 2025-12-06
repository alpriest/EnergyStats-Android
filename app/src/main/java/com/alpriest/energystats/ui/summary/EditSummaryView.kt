package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.helpers.MonthPicker
import com.alpriest.energystats.ui.helpers.YearPicker
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

sealed class SummaryDateRange {
    object Automatic : SummaryDateRange()
    data class Manual(val from: MonthYear, val to: MonthYear) : SummaryDateRange()
}

data class SummaryDateRangeSerialised(
    var automatic: Boolean,
    var from: MonthYear?,
    var to: MonthYear?
)

data class MonthYear(
    var month: Int, // zero-based
    var year: Int
)

data class EditSummaryViewData(
    val automatic: Boolean,
    val fromYear: Int,
    val fromMonth: Int,
    val toYear: Int,
    val toMonth: Int
)

class EditSummaryView(
    private val configManager: ConfigManaging,
    private val navController: NavHostController,
    private val onChange: (SummaryDateRange) -> Unit
) {
    @Composable
    fun Content(
        viewModel: EditSummaryViewModel = viewModel(
            factory = EditSummaryViewModelFactory(
                configManager,
                onChange
            )
        ),
    ) {
        val viewData = viewModel.viewDataStream.collectAsStateWithLifecycle().value

        val textColor = if (viewData.automatic) {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.onBackground
        }

        ContentWithBottomButtonPair(
            navController,
            onConfirm = {
                navController.popBackStack()
                viewModel.save()
            },
            dirtyStateFlow = viewModel.dirtyState,
            content = { innerModifier ->
                SettingsPage(innerModifier) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SettingsCheckbox(title = "Automatic", state = mutableStateOf(viewData.automatic), onUpdate = { viewModel.didChangeAutomatic(it) })

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "From",
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )

                            MonthPicker(month = viewData.fromMonth, enabled = !viewData.automatic) { viewModel.didChangeFromMonth(it) }
                            YearPicker(year = viewData.fromYear, enabled = !viewData.automatic) { viewModel.didChangeFromYear(it) }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "To",
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )

                            MonthPicker(month = viewData.toMonth, enabled = !viewData.automatic) { viewModel.didChangeToMonth(it) }
                            YearPicker(year = viewData.toYear, enabled = !viewData.automatic) { viewModel.didChangeToYear(it) }
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditSummaryViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        val viewModel = EditSummaryViewModel(
            FakeConfigManager(),
            { }
        )
        EditSummaryView(
            FakeConfigManager(),
            navController = NavHostController(LocalContext.current),
            { }
        ).Content(viewModel)
    }
}