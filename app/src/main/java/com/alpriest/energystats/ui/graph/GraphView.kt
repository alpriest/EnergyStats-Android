package com.alpriest.energystats.ui.graph

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.model.LineData

@Composable
fun GraphView() {
    Row(modifier = Modifier.fillMaxWidth()) {
        LineChart(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
                .padding(32.dp),
            color = MaterialTheme.colors.primaryVariant,
            lineData = listOf(
                LineData(50, 50F),
                LineData(60, 60F),
                LineData(70, 70F)
            ),
            axisConfig = AxisConfig(
                showAxis = true,
                isAxisDashed = false,
                showUnitLabels = true,
                showXLabels = true,
                xAxisColor = MaterialTheme.colors.onSurface,
                yAxisColor = MaterialTheme.colors.onSurface,
                textColor = Color.Black
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GraphViewPreview() {
    EnergyStatsTheme {
        GraphView()
    }
}