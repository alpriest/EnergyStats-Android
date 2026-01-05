package com.alpriest.energystats.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HouseView(modifier: Modifier = Modifier, foregroundColor: Color, backgroundColor: Color) {
    Canvas(
        modifier = modifier
    ) {
        val cornerRadius = size.width / 24f
        val roofMargin = 2f
        val soffitsTop = (size.height * 0.4f) + cornerRadius
        val houseMargin = (size.width * 0.1f) + (roofMargin * 2.0f)
        val roofHeight = size.height * 0.33f

        // House top
        val houseSize = Size(size.width - (2.0f * houseMargin), size.height - soffitsTop)
        drawRect(
            color = backgroundColor,
            topLeft = Offset(houseMargin, soffitsTop),
            size = Size(houseSize.width, houseSize.height / 2.0f)
        )
        // House bottom
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(houseMargin, soffitsTop),
            size = houseSize,
            cornerRadius = CornerRadius(x = cornerRadius, y = cornerRadius)
        )

        // Roof
        val trianglePath: Path = Path().apply {
            moveTo(houseMargin, soffitsTop + 2.0f)
            lineTo(houseMargin, soffitsTop)
            lineTo(houseMargin + (houseSize.width / 2.0f), soffitsTop - roofHeight)
            lineTo(size.width - houseMargin, soffitsTop)
            lineTo(size.width - houseMargin, soffitsTop + 2.0f)
            close()
        }
        drawPath(
            color = backgroundColor,
            path = trianglePath
        )

        // Roof Line
        val bottomLeftRoof = Offset(roofMargin, soffitsTop)
        val bottomRightRoof = Offset(size.width - roofMargin, soffitsTop)
        val roofApex = Offset(size.width / 2.0f, roofMargin)
        val roofPath = Path().apply {
            moveTo(bottomLeftRoof.x, bottomLeftRoof.y)
            lineTo(roofApex.x, roofApex.y)
            lineTo(bottomRightRoof.x, bottomRightRoof.y)
        }
        drawPath(
            color = backgroundColor,
            path = roofPath,
            style = Stroke(width = 5f, pathEffect = PathEffect.cornerPathEffect(5f), cap = StrokeCap.Round)
        )

        // Door
        val doorWidth = size.width * 0.23f
        val doorLeft = size.width * 0.5f - (doorWidth / 2.0f)
        val doorHeight = soffitsTop * 0.9f
        drawRoundRect(
            color = foregroundColor,
            topLeft = Offset(x = doorLeft, y = size.height - (soffitsTop * 0.13f) - doorHeight),
            size = Size(doorWidth, doorHeight),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Chimney
        val chimneyWidth = size.width * 0.05f
        val chimneyHeight = size.height * 0.18f

        val chimneyPath = Path().apply {
            moveTo(houseMargin + houseSize.width - chimneyWidth, soffitsTop - chimneyHeight - size.height * 0.115f)
            lineTo(houseMargin + houseSize.width, soffitsTop - chimneyHeight - size.height * 0.115f)
            lineTo(houseMargin + houseSize.width, soffitsTop - chimneyHeight + (size.height * 0.09f))
            lineTo(houseMargin + houseSize.width - chimneyWidth, soffitsTop - chimneyHeight + (size.height * 0.05f))
            close()
        }
        drawPath(
            color = backgroundColor,
            path = chimneyPath,
        )
    }
}

@Preview
@Composable
fun HouseViewPreviewWatch() {
    HouseView(
        modifier = Modifier
            .height(30.dp)
            .width(30.dp * 1.3f),
        Color.Black,
        Color.White
    )
}

@Preview
@Composable
fun HouseViewPreviewPhone() {
    HouseView(
        modifier = Modifier
            .height(60.dp)
            .width(60.dp * 1.3f),
        Color.Black,
        Color.White
    )
}
