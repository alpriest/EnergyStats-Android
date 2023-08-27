package com.alpriest.energystats.ui.flow.inverter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

val Colors.InverterBackground: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color.White else Color.Black

val Colors.BoltTint: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color.Black else Color.White

@Composable
fun InverterIconView(modifier: Modifier = Modifier) {
    val painter = rememberVectorPainter(Icons.Default.ElectricBolt)
    val inverterBackground = colors.InverterBackground
    val boltTint = colors.BoltTint

    Canvas(
        modifier = modifier
    ) {
        val cablesHeight = size.height * 0.12f
        val cablesWidth = size.width * 0.1f
        val panelX = size.width * 0.15f
        val panelY = size.height * 0.2f
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
            color = Color.Black,
            style = Stroke(width = inverterLineWidth),
            cornerRadius = cornerSize
        )

        // Cables
        drawRect(
            topLeft = Offset(x = cablesWidth * 1.5f, y = size.height - cablesHeight - cablesLineWidth),
            size =  Size(width = cablesWidth, height = cablesHeight),
            color = inverterBackground,
            style = Stroke(width = cablesLineWidth)
        )

        drawRect(
            topLeft = Offset(x = cablesWidth * 3.5f, y = size.height - cablesHeight - cablesLineWidth),
            size =  Size(width = cablesWidth, height = cablesHeight),
            color = inverterBackground,
            style = Stroke(width = cablesLineWidth)
        )

        // Panel
        drawRect(
            topLeft = Offset(x = panelX, y = panelY),
            size = Size(width = panelX * 2.3f, height = panelY * 1.3f),
            color = Color.Gray.copy(alpha = 0.5f)
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

@Preview(showBackground = true, widthDp = 200, heightDp = 300)
@Composable fun InverterViewPreview() {
    Column {
        EnergyStatsTheme(darkTheme = true) {
            InverterIconView(modifier = Modifier.width(50.dp).height(65.dp))
        }
    }
}