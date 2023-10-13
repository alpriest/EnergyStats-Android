package com.alpriest.energystats.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryWidget(0.4f)
}

class BatteryWidget(val amount: Float) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BatteryWidgetContent(amount)
        }
    }
}

@Composable
fun BatteryWidgetContent(amount: Float) {
    Column(modifier = GlanceModifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f))) {
        Box(
            modifier = GlanceModifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(bitmap = gauge(0.4f).asAndroidBitmap()),
                contentDescription = null
            )

            Text(
                formatAsPercentage(amount),
                style = TextStyle(
                    textAlign = TextAlign.Center
                )
            )
        }
        Text(
            "Full in 1 hour",
            GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                textAlign = TextAlign.Center
            )
        )
    }
}

fun formatAsPercentage(floatValue: Float): String {
    return "${String.format("%.0f", floatValue * 100)}%"
}

fun gauge(percentage: Float): ImageBitmap {
    val strokeWidth = 15f
    val width = 140
    val height = 140
    val bitmap = ImageBitmap(width, height)
    val canvas = Canvas(bitmap)
    val trackPaint = Paint()
    trackPaint.style = PaintingStyle.Stroke
    trackPaint.strokeCap = StrokeCap.Round
    trackPaint.strokeWidth = strokeWidth
    trackPaint.color = Color.LightGray

    val progressPaint = Paint()
    progressPaint.style = PaintingStyle.Stroke
    progressPaint.strokeCap = StrokeCap.Round
    progressPaint.strokeWidth = strokeWidth
    progressPaint.color = Color.White

    val rect = Rect(
        Offset(strokeWidth / 2.0f, strokeWidth / 2.0f),
        Offset(width.toFloat() - strokeWidth / 2.0f, height.toFloat() - strokeWidth / 2.0f)
    )

    canvas.drawArc(
        rect,
        140f,
        260f,
        false,
        trackPaint
    )

    canvas.drawArc(
        rect,
        140f,
        percentage * 260f,
        false,
        progressPaint
    )

//    val angleInDegrees = (percentage * 260.0) + 50.0
//    val radius = (rect.height / 2)
//    val x = -(radius * sin(Math.toRadians(angleInDegrees))).toFloat() + (rect.width / 2)
//    val y = (radius * cos(Math.toRadians(angleInDegrees))).toFloat() + (rect.height / 2)
//
//    canvas.drawCircle(
//        paint = trackPaint,
//        radius = strokeWidth / 3.0f,
//        center = Offset(x, y)
//    )

    return bitmap
}

//
//@Composable
//fun ComposeCircularProgressBar(
//    modifier: Modifier = Modifier,
//    percentage: Float,
//    fillColor: Color,
//    backgroundColor: Color,
//    strokeWidth: Dp
//) {
//    Canvas(
//        modifier = modifier
//            .padding(10.dp)
//    ) {
//        // Background Line
//        drawArc(
//            color = backgroundColor,
//            140f,
//            260f,
//            false,
//            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
//            size = Size(size.width, size.height)
//        )
//
//        drawArc(
//            color = fillColor,
//            140f,
//            percentage * 260f,
//            false,
//            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
//            size = Size(size.width, size.height)
//        )
//
//        val angleInDegrees = (percentage * 260.0) + 50.0
//        val radius = (size.height / 2)
//        val x = -(radius * sin(Math.toRadians(angleInDegrees))).toFloat() + (size.width / 2)
//        val y = (radius * cos(Math.toRadians(angleInDegrees))).toFloat() + (size.height / 2)
//
//        drawCircle(
//            color = Color.White,
//            radius = strokeWidth.toPx() / 3.0f,
//            center = Offset(x, y)
//        )
//    }
//}