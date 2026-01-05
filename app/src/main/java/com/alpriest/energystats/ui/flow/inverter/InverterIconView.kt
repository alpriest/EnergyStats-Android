package com.alpriest.energystats.ui.flow.inverter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.shared.ui.iconBackgroundColor
import com.alpriest.energystats.shared.ui.iconForegroundColor
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun InverterIconView(modifier: Modifier = Modifier, themeStream: MutableStateFlow<AppTheme>) {
    val painter = rememberVectorPainter(Icons.Default.ElectricBolt)
    val inverterBackground = iconBackgroundColor(isDarkMode(themeStream))
    val boltTint = iconForegroundColor(isDarkMode(themeStream))

    Canvas(
        modifier = modifier
    ) {
        val cablesHeight = size.height * 0.12f
        val cablesWidth = size.width * 0.1f
        val cornerSize = CornerRadius(x = 8f, y = 8f)
        val inverterLineWidth = 5f
        val cablesLineWidth = 5f
        val boltSize = Size(size.width / 2.5f, size.width / 2f)

        // Inverter box
        drawRoundRect(
            topLeft = Offset(x = inverterLineWidth / 2.0f, y = inverterLineWidth / 2.0f),
            size = Size(width = size.width - inverterLineWidth, height = size.height - inverterLineWidth - cablesHeight),
            color = inverterBackground,
            style = Fill,
        )

        drawRoundRect(
            topLeft = Offset(x = inverterLineWidth / 2.0f, y = inverterLineWidth / 2.0f),
            size = Size(width = size.width - inverterLineWidth, height = size.height - inverterLineWidth - cablesHeight),
            color = inverterBackground,
            style = Stroke(width = inverterLineWidth),
            cornerRadius = cornerSize
        )

        // Cables
        drawRect(
            topLeft = Offset(x = cablesWidth * 1.5f, y = size.height - cablesHeight - cablesLineWidth),
            size = Size(width = cablesWidth, height = cablesHeight),
            color = inverterBackground,
            style = Stroke(width = cablesLineWidth)
        )

        drawRect(
            topLeft = Offset(x = cablesWidth * 3.5f, y = size.height - cablesHeight - cablesLineWidth),
            size = Size(width = cablesWidth, height = cablesHeight),
            color = inverterBackground,
            style = Stroke(width = cablesLineWidth)
        )

        with(painter) {
            translate(left = (size.width / 2.2f) + 15f, top = 15f) {
                draw(
                    size = boltSize,
                    colorFilter = ColorFilter.tint(boltTint)
                )
            }
        }
    }
}

@Preview(showBackground = false, widthDp = 200, heightDp = 300)
@Composable
fun InverterViewPreview() {
    Column {
        EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
            InverterIconView(
                modifier = Modifier
                    .width(50.dp)
                    .height(65.dp),
                MutableStateFlow(AppTheme.demo())
            )
        }
    }
}