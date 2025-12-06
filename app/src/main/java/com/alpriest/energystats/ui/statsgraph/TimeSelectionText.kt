package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TimeSelectionText(viewModel: StatsTabViewModel) {
    val selectedValues = viewModel.valuesAtTimeStream.collectAsState().value
    val selectedDateTime = selectedValues.firstOrNull()?.periodDescription

    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        modifier = Modifier.Companion
            .padding(vertical = 8.dp)
            .fillMaxWidth()

    ) {
        viewModel.lastMarkerModelStream.value?.let {
            selectedDateTime?.let {
                Row(modifier = Modifier.Companion.clickable {
                    viewModel.lastMarkerModelStream.value = null
                    viewModel.valuesAtTimeStream.value = listOf()
                }) {
                    Text(
                        text = it.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.Companion.padding(end = 6.dp)
                    )

                    Text(
                        text = stringResource(R.string.clear),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } ?: run {
            Text(
                stringResource(R.string.touch_the_graph_to_see_values_at_that_time),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}