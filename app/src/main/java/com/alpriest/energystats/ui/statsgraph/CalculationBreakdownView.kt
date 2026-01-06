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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.models.CalculationBreakdown
import com.alpriest.energystats.shared.models.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun CalculationBreakdownView(visible: Boolean, calculationBreakdown: CalculationBreakdown, themeStream: MutableStateFlow<AppTheme>) {
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            Modifier
                .padding(bottom = 8.dp)
                .background(
                    ApproximationBackground(themeStream),
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = ApproximationHeader(themeStream),
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            Text(
                calculationBreakdown.formula,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )
            Text(
                calculationBreakdown.calculation(decimalPlaces),
                fontSize = 12.sp
            )
        }
    }
}