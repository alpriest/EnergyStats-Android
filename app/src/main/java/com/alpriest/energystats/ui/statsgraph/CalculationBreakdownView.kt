package com.alpriest.energystats.ui.statsgraph

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.CalculationBreakdown

@Composable
fun CalculationBreakdownView(visible: Boolean, calculationBreakdown: CalculationBreakdown, fontSize: TextUnit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            Modifier
                .padding(bottom = 8.dp)
                .background(
                    MaterialTheme.colors.ApproximationBackground,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = ApproximationHeader,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            Text(
                calculationBreakdown.formula,
                fontSize = fontSize,
                fontStyle = FontStyle.Italic
            )
            Text(
                calculationBreakdown.calculation,
                fontSize = fontSize
            )
        }
    }
}