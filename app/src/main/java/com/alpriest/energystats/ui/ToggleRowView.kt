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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.models.GraphBounds
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.ui.DimmedTextColor
import com.alpriest.energystats.shared.ui.roundedToString
import com.alpriest.energystats.ui.paramsgraph.GraphVariable
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T : GraphVariable> ToggleRowView(
    it: T,
    appSettingsStream: StateFlow<AppSettings>,
    toggleVisibility: (T) -> Unit,
    title: String,
    description: String?,
    value: String?,
    boundsValue: GraphBounds?
) {
    val textColor = if (it.enabled) MaterialTheme.colorScheme.onBackground else DimmedTextColor
    val appSettings = appSettingsStream.collectAsState().value
    val fontSize = appSettings.fontSize()
    val colour = it.colour(appSettingsStream).copy(alpha = if (it.enabled) 1.0f else 0.5f)

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(bottom = 5.dp)
            .clickable {
                toggleVisibility(it)
            }
    ) {
        Box(modifier = Modifier.padding(top = if (appSettings.useLargeDisplay) 10.dp else 1.dp)) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(
                    color = colour,
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
                Column(
                    modifier = Modifier.weight(1.0f)
                ) {
                    Text(
                        title,
                        color = textColor,
                        fontSize = fontSize
                    )

                    if (appSettings.showGraphValueDescriptions) {
                        description?.let {
                            Text(
                                it,
                                color = DimmedTextColor,
                                fontSize = appSettings.smallFontSize()
                            )
                        }
                    }
                }

                value?.let {
                    Text(
                        it,
                        color = textColor,
                        fontSize = fontSize,
                    )
                }

                boundsValue?.let {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            it.min.toDouble().roundedToString(appSettings.decimalPlaces),
                            color = textColor,
                            fontSize = fontSize,
                        )
                        Text(
                            "MIN",
                            fontSize = 8.sp
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            it.max.toDouble().roundedToString(appSettings.decimalPlaces),
                            color = textColor,
                            fontSize = fontSize,
                        )
                        Text(
                            "MAX",
                            fontSize = 8.sp
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            it.now.toDouble().roundedToString(appSettings.decimalPlaces),
                            color = textColor,
                            fontSize = fontSize,
                        )
                        Text(
                            "NOW",
                            fontSize = 8.sp
                        )
                    }
                }
            }

        }
    }
}