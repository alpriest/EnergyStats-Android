package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.helpers.MonthPicker
import com.alpriest.energystats.ui.helpers.YearPicker
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.Typography
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

sealed class DatePickerRange {
    object DAY : DatePickerRange()
    object MONTH : DatePickerRange()
    object YEAR : DatePickerRange()
    data class CUSTOM(val start: LocalDate, val end: LocalDate) : DatePickerRange()

    fun isCustom(): Boolean {
        return !(this == DAY || this == MONTH || this == YEAR)
    }
}

class StatsDatePickerHeaderViewModelFactory(
    private val displayModeStream: MutableStateFlow<StatsDisplayMode>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatsDatePickerHeaderViewModel(displayModeStream) as T
    }
}

class StatsDatePickerHeaderView(private val displayModeStream: MutableStateFlow<StatsDisplayMode>) {
    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        viewModel: StatsDatePickerHeaderViewModel = viewModel(factory = StatsDatePickerHeaderViewModelFactory(displayModeStream)),
        graphShowingState: MutableStateFlow<Boolean>,
    ) {
        val range = viewModel.rangeStream.collectAsState().value
        val month = viewModel.monthStream.collectAsState().value
        val year = viewModel.yearStream.collectAsState().value
        val canIncrease = viewModel.canIncreaseStream.collectAsState().value
        val canDecrease = viewModel.canDecreaseStream.collectAsState().value

        Row(modifier = modifier) {
            DateRangeMenu(viewModel, range, graphShowingState)

            when (range) {
                is DatePickerRange.DAY -> com.alpriest.energystats.ui.helpers.CalendarView(viewModel.dateStream, style = Typography.headlineMedium)
                is DatePickerRange.MONTH -> {
                    MonthPicker(month) { viewModel.monthStream.value = it }
                    YearPicker(year) { viewModel.yearStream.value = it }
                }

                is DatePickerRange.YEAR -> YearPicker(year) { viewModel.yearStream.value = it }
                is DatePickerRange.CUSTOM -> CustomRangePicker(viewModel)
            }

            Spacer(modifier = Modifier.weight(1.0f))

            if (!range.isCustom()) {
                ESButton(
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .padding(vertical = 6.dp)
                        .size(36.dp),
                    onClick = { viewModel.decrease() },
                    contentPadding = PaddingValues(0.dp),
                    enabled = canDecrease
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Left",
                        modifier = Modifier.size(32.dp)
                    )
                }

                ESButton(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .size(36.dp),
                    onClick = { viewModel.increase() },
                    contentPadding = PaddingValues(0.dp),
                    enabled = canIncrease
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Right",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeMenu(
    viewModel: StatsDatePickerHeaderViewModel,
    range: DatePickerRange,
    graphShowingState: MutableStateFlow<Boolean>
) {
    var showing by remember { mutableStateOf(false) }
    val graphShowing = graphShowingState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .padding(end = 14.dp)
            .padding(start = 4.dp)
    ) {
        ESButton(
            onClick = { showing = true },
            modifier = Modifier
                .padding(vertical = 4.dp)
                .size(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = showing,
            onDismissRequest = { showing = false }
        ) {
            DropdownMenuItem(onClick = {
                viewModel.rangeStream.value = DatePickerRange.DAY
                showing = false
            }, text = {
                Text(stringResource(R.string.day))
            }, trailingIcon = {
                if (range == DatePickerRange.DAY) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })
            HorizontalDivider()
            DropdownMenuItem(onClick = {
                viewModel.rangeStream.value = DatePickerRange.MONTH
                showing = false
            }, text = {
                Text(stringResource(R.string.month))
            }, trailingIcon = {
                if (range == DatePickerRange.MONTH) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })
            HorizontalDivider()
            DropdownMenuItem(onClick = {
                viewModel.rangeStream.value = DatePickerRange.YEAR
                showing = false
            }, text = {
                Text(stringResource(R.string.year))
            }, trailingIcon = {
                if (range == DatePickerRange.YEAR) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })
            HorizontalDivider()
            DropdownMenuItem(onClick = {
                showBottomSheet = true
                showing = false
            }, text = {
                Text(stringResource(R.string.custom_range))
            }, trailingIcon = {
                if (range.isCustom()) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })

            HorizontalDivider(thickness = 4.dp)
            DropdownMenuItem(onClick = {
                graphShowingState.value = !graphShowing.value
                showing = false
            }, text = {
                Text(if (graphShowing.value) stringResource(R.string.hide_graph) else stringResource(R.string.show_graph))
            }, trailingIcon = {
                Icon(imageVector = Icons.Default.BarChart, contentDescription = "graph")
            })
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                contentWindowInsets = { WindowInsets.safeDrawing }
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    CustomDateRangePickerView()
                }
            }
        }
    }
}

@Composable
private fun CustomRangePicker(viewModel: StatsDatePickerHeaderViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.alpriest.energystats.ui.helpers.CalendarView(viewModel.customStartDate, style = Typography.bodyMedium)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.padding(end = 14.dp),
            tint = Color.White
        )
        com.alpriest.energystats.ui.helpers.CalendarView(viewModel.customEndDate, style = Typography.bodyMedium)
    }
}

@Preview(widthDp = 500, heightDp = 500)
@Composable
fun StatsDatePickerViewPreview() {
    StatsDatePickerHeaderView(MutableStateFlow(StatsDisplayMode.Day(LocalDate.now()))).Content(
        graphShowingState = MutableStateFlow(false)
    )
}