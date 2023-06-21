package com.alpriest.energystats.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.ui.statsgraph.GraphVariable
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun <T : GraphVariable> ToggleRowView(
    it: T,
    themeStream: MutableStateFlow<AppTheme>,
    toggleVisibility: (T) -> Unit,
    title: String,
    description: String,
    value: Double?
) {
    val textColor = if (it.enabled) MaterialTheme.colors.onBackground else DimmedTextColor
    val appTheme = themeStream.collectAsState().value
    val fontSize = appTheme.fontSize()
    val decimalPlaces = appTheme.decimalPlaces

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(bottom = 6.dp)
            .clickable {
                toggleVisibility(it)
            }
    ) {
        Box(modifier = Modifier.padding(top = if (appTheme.useLargeDisplay) 10.dp else 4.dp)) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(
                    color = it.colour.copy(alpha = if (it.enabled) 1.0f else 0.5f),
                    radius = size.minDimension / 2,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    color = textColor,
                    fontSize = fontSize
                )

                Text(
                    value?.kWh(decimalPlaces) ?: "",
                    color = textColor,
                    fontSize = fontSize,
                )
            }

            Text(
                description,
                color = DimmedTextColor,
                fontSize = appTheme.smallFontSize()
            )
        }
    }
}