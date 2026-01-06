package com.alpriest.energystats.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.alpriest.energystats.MainActivity
import com.alpriest.energystats.shared.models.WidgetTapAction
import com.alpriest.energystats.shared.ui.Sunny
import com.alpriest.energystats.ui.theme.Green
import com.alpriest.energystats.ui.theme.Red
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                BatteryDataRepository.getInstance().update(context)
            } catch (_: Exception) {
            }
        }
    }
}

class BatteryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = BatteryDataRepository.getInstance()
        repository.updateFromSharedConfig(context)

        provideContent {
            BatteryWidgetContent(repository.batteryPercentage, repository.chargeDescription, repository.tapAction)
        }
    }
}

@Composable
fun BatteryWidgetContent(amount: Float, chargeDescription: String?, tapAction: WidgetTapAction) {
    val launchIntent = Intent(LocalContext.current, MainActivity::class.java)
    val action: Action = when (tapAction) {
        WidgetTapAction.Launch -> actionStartActivity(launchIntent)
        WidgetTapAction.Refresh -> actionRunCallback<BatteryRefreshAction>()
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = action)
    ) {
        Box(
            modifier = GlanceModifier.fillMaxWidth()
                .padding(top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Image(
                    provider = ImageProvider(bitmap = gauge(amount).asAndroidBitmap()),
                    contentDescription = null
                )

                Image(
                    provider = ImageProvider(bitmap = drawBattery().asAndroidBitmap()),
                    contentDescription = null
                )
            }

            Text(
                formatAsPercentage(amount),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    color = ColorProvider(Color.White)
                )
            )
        }

        chargeDescription?.let {
            Text(
                it,
                GlanceModifier.fillMaxWidth().padding(top = 2.dp),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = ColorProvider(Color.White)
                ),
            )
        }
    }
}

class BatteryRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        BatteryDataRepository.getInstance().update(context)
    }
}

fun formatAsPercentage(floatValue: Float): String {
    return "${String.format("%.0f", floatValue * 100)}%"
}

fun gauge(percentage: Float): ImageBitmap {
    val strokeWidth = 24f
    val width = 180
    val height = 180
    val bitmap = ImageBitmap(width, height)
    val canvas = Canvas(bitmap)
    val trackPaint = Paint()
    trackPaint.style = PaintingStyle.Stroke
    trackPaint.strokeCap = StrokeCap.Round
    trackPaint.strokeWidth = strokeWidth
    trackPaint.color = Color.White.copy(alpha = 0.3f)

    val progressPaint = Paint()
    progressPaint.style = PaintingStyle.Stroke
    progressPaint.strokeCap = StrokeCap.Round
    progressPaint.strokeWidth = strokeWidth
    progressPaint.color = when (percentage) {
        in 0f..0.4f -> Red
        in 0.4f..0.7f -> Sunny
        else -> Green
    }

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

    return bitmap
}

fun drawBattery(): ImageBitmap {
    val height = 40f
    val size = Size(height * 1.25f, height)
    val boxTop = (size.height * 0.11f)
    val terminalInset = size.width * 0.2f
    val terminalWidth = size.width * 0.2f
    val barHeight = 4f
    val halfBarHeight = barHeight / 2f

    val whitePaint = Paint()
    whitePaint.color = Color.White
    val blackPaint = Paint()
    blackPaint.color = Color.Black
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = Canvas(bitmap)

    fun drawRoundRect(paint: Paint, topLeft: Offset, size: Size, cornerRadius: CornerRadius) {
        canvas.drawRoundRect(
            left = topLeft.x,
            top = topLeft.y,
            bottom = topLeft.y + size.height,
            paint = paint,
            radiusX = cornerRadius.x,
            radiusY = cornerRadius.y,
            right = topLeft.x + size.width
        )
    }

    // Battery
    val batterySize = Size(size.width, size.height - boxTop)
    drawRoundRect(
        paint = whitePaint,
        topLeft = Offset(x = 0f, y = boxTop),
        size = batterySize,
        cornerRadius = CornerRadius(x = 5f, y = 5f)
    )

    // Negative terminal
    drawRoundRect(
        paint = whitePaint,
        topLeft = Offset(x = terminalInset, y = 0f),
        size = Size(terminalWidth, boxTop + barHeight),
        cornerRadius = CornerRadius(x = 3f, y = 3f)
    )

    // Positive terminal
    drawRoundRect(
        paint = whitePaint,
        topLeft = Offset(x = size.width - 2 * terminalInset, y = 0f),
        size = Size(terminalWidth, boxTop + barHeight),
        cornerRadius = CornerRadius(x = 3f, y = 3f)
    )

    // Minus
    drawRoundRect(
        paint = blackPaint,
        topLeft = Offset(x = terminalInset, y = boxTop + batterySize.height / 2.0f),
        size = Size(terminalWidth, barHeight),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Plus
    drawRoundRect(
        paint = blackPaint,
        topLeft = Offset(
            x = size.width - 2 * terminalInset,
            y = boxTop + batterySize.height / 2.0f
        ),
        size = Size(terminalWidth, barHeight),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        paint = blackPaint,
        topLeft = Offset(
            x = size.width - 2 * terminalInset + (terminalInset / 2f) - halfBarHeight,
            y = boxTop + batterySize.height / 2.0f - (terminalInset / 2f) + halfBarHeight
        ),
        size = Size(barHeight, terminalWidth),
        cornerRadius = CornerRadius(2f, 2f)
    )

    return bitmap
}
