package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.statsgraph.MonthPicker
import com.alpriest.energystats.ui.statsgraph.YearPicker
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.time.LocalDate

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

@Composable
fun EditSummaryView(
    modifier: Modifier,
    navController: NavController,
    viewModel: SummaryTabViewModel,
) {
    val automatic = remember { mutableStateOf(viewModel.summaryDateRangeStream.value == SummaryDateRange.Automatic) }
    val fromMonth = remember { mutableIntStateOf(0) }
    val fromYear = remember { mutableIntStateOf(0) }
    val toMonth = remember { mutableIntStateOf(0) }
    val toYear = remember { mutableIntStateOf(0) }
    val summaryDateRange = viewModel.summaryDateRangeStream.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(summaryDateRange) {
        when (summaryDateRange) {
            SummaryDateRange.Automatic -> {
                automatic.value = true
                fromMonth.intValue = 1
                fromYear.intValue = 2020
                toMonth.intValue = LocalDate.now().monthValue
                toYear.intValue = LocalDate.now().year
            }

            is SummaryDateRange.Manual -> {
                automatic.value = false
                fromMonth.intValue = summaryDateRange.from.month
                fromYear.intValue = summaryDateRange.from.year
                toMonth.intValue = summaryDateRange.to.month
                toYear.intValue = summaryDateRange.to.year
            }
        }
    }

    val textColor = if (automatic.value) {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    ContentWithBottomButtonPair(navController, modifier = modifier, onConfirm = {
        val updatedSummaryDateRange = if (automatic.value) {
            SummaryDateRange.Automatic
        } else {
            SummaryDateRange.Manual(
                from = MonthYear(fromMonth.intValue, fromYear.intValue),
                to = MonthYear(toMonth.intValue, toYear.intValue)
            )
        }
        navController.popBackStack()
        viewModel.setDateRange(updatedSummaryDateRange, context)
    },
        content = { innerModifier ->
            SettingsPage(innerModifier) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SettingsCheckbox(title = "Automatic", state = automatic, onUpdate = { automatic.value = it })

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "From",
                            modifier = Modifier.weight(1f),
                            color = textColor
                        )

                        MonthPicker(month = fromMonth.intValue, enabled = !automatic.value) { fromMonth.intValue = it }
                        YearPicker(year = fromYear.intValue, enabled = !automatic.value) { fromYear.intValue = it }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "To",
                            modifier = Modifier.weight(1f),
                            color = textColor
                        )

                        MonthPicker(month = toMonth.intValue, enabled = !automatic.value) { toMonth.intValue = it }
                        YearPicker(year = toYear.intValue, enabled = !automatic.value) { toYear.intValue = it }
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EditSummaryViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        EditSummaryView(
            Modifier,
            navController = NavHostController(LocalContext.current),
            SummaryTabViewModel(
                DemoNetworking(),
                FakeConfigManager()
            )
        )
    }
}