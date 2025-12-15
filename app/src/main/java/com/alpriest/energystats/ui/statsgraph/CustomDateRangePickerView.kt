package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.ui.helpers.SegmentedControl
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsSegmentedControl
import com.alpriest.energystats.ui.theme.ESButton
import java.time.LocalDate

enum class CustomDateRangeDisplayUnit {
    DAYS,
    MONTHS;

    val title: String
        get() = when (this) {
            DAYS -> "Days"
            MONTHS -> "Months"
        }
}

@Composable
fun CustomDateRangePickerView() {
    var start = remember { LocalDate.now() }
    var end = remember { LocalDate.now() }
    val context = LocalContext.current
    var viewBy = remember { CustomDateRangeDisplayUnit.MONTHS }

    SettingsPage {
        SettingsColumn(header = "Quick choice") {
            Row {
                ESButton(
                    onClick = {
                        start = LocalDate.now().minusMonths(12)
                        end = LocalDate.now()
                    },
                    content = {
                        Text("Last 12 months")
                    }
                )
            }

            Row {
                ESButton(
                    onClick = {
                        start = LocalDate.now().minusMonths(6)
                        end = LocalDate.now()
                    },
                    content = {
                        Text("Last 6 months")
                    }
                )
            }
        }

        SettingsColumn(header = "View by") {
            SettingsSegmentedControl(
                segmentedControl = {
                    val items = CustomDateRangeDisplayUnit.entries
                    SegmentedControl(
                        items = items.map { it.title },
                        defaultSelectedItemIndex = items.indexOf(viewBy),
                        color = colorScheme.primary
                    ) {
                        viewBy = items[it]
                    }
                }
            )
        }
//    viewModel.rangeStream.value = DatePickerRange.CUSTOM(LocalDate.now().minusDays(30), LocalDate.now())
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomDateRangePickerViewPreview() {
    CustomDateRangePickerView()
}
