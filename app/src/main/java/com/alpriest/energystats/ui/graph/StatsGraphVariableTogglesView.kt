package com.alpriest.energystats.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.theme.DimmedTextColor

@Composable
fun StatsGraphVariableTogglesView(viewModel: StatsGraphTabViewModel, modifier: Modifier = Modifier) {
    Column(modifier) {
        viewModel.variables.map {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(bottom = 6.dp)
                    .clickable {
                        // TODO toggle the data visibility
                    }
            ) {
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawCircle(
                            color = it.colour(),
                            radius = size.minDimension / 2,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row {
                        Text(
                            it.title(),
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(viewModel.totalOf(it).kWh(2))
                    }

                    Text(
                        it.description(),
                        color = DimmedTextColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun StatsGraphVariableTogglesViewPreview() {
    StatsGraphVariableTogglesView(StatsGraphTabViewModel(FakeConfigManager(), DemoNetworking()))
}