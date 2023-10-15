//package com.alpriest.energystats.widget
//
//import android.appwidget.AppWidgetManager
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Canvas
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.Paint
//import androidx.compose.ui.graphics.PaintingStyle
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.asAndroidBitmap
//import androidx.glance.GlanceId
//import androidx.glance.GlanceModifier
//import androidx.glance.Image
//import androidx.glance.ImageProvider
//import androidx.glance.action.ActionParameters
//import androidx.glance.action.clickable
//import androidx.glance.appwidget.GlanceAppWidget
//import androidx.glance.appwidget.GlanceAppWidgetReceiver
//import androidx.glance.appwidget.action.ActionCallback
//import androidx.glance.appwidget.action.actionRunCallback
//import androidx.glance.appwidget.provideContent
//import androidx.glance.background
//import androidx.glance.layout.Alignment
//import androidx.glance.layout.Box
//import androidx.glance.layout.Column
//import androidx.glance.layout.fillMaxSize
//import androidx.glance.layout.fillMaxWidth
//import androidx.glance.text.Text
//import androidx.glance.text.TextAlign
//import androidx.glance.text.TextStyle
//import com.alpriest.energystats.ui.theme.Green
//import com.alpriest.energystats.ui.theme.Red
//import com.alpriest.energystats.ui.theme.Sunny
//import kotlinx.coroutines.runBlocking
//
//class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
//    override val glanceAppWidget: GlanceAppWidget = BatteryWidget()
//
//    override fun onEnabled(context: Context?) {
//        super.onEnabled(context)
//
//        Log.d("AWP", "AWP widget created")
//    }
//
//    override fun onDisabled(context: Context?) {
//        super.onDisabled(context)
//
//        Log.d("AWP", "AWP widget deleted")
//    }
//
//    override fun onReceive(context: Context, intent: Intent) {
//        super.onReceive(context, intent)
//
//    }
//
//    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
//        super.onUpdate(context, appWidgetManager, appWidgetIds)
//
//        Log.d("AWP", "AWP widget onUpdate")
//        runBlocking {
//            LatestDataRepository.getInstance().update(context)
//        }
//    }
//}
//
//class BatteryWidget : GlanceAppWidget() {
//    override suspend fun provideGlance(context: Context, id: GlanceId) {
//        provideContent {
//            val battery = LatestDataRepository.getInstance().batteryPercentage
//
//            BatteryWidgetContent(battery)
//        }
//    }
//}
//
//@Composable
//fun BatteryWidgetContent(amount: Float) {
//    Column(
//        modifier = GlanceModifier
//            .fillMaxSize()
//            .background(Color.White.copy(alpha = 0.1f))
//            .clickable(onClick = actionRunCallback<RefreshAction>())
//    ) {
//        Box(
//            modifier = GlanceModifier.fillMaxWidth(),
//            contentAlignment = Alignment.Center
//        ) {
//            Box(contentAlignment = Alignment.BottomCenter) {
//                Image(
//                    provider = ImageProvider(bitmap = gauge(amount).asAndroidBitmap()),
//                    contentDescription = null
//                )
//
//                Image(
//                    provider = ImageProvider(bitmap = drawBattery().asAndroidBitmap()),
//                    contentDescription = null
//                )
//            }
//
//            Text(
//                formatAsPercentage(amount),
//                style = TextStyle(
//                    textAlign = TextAlign.Center
//                )
//            )
//        }
//        Text(
//            "Full in 1 hour",
//            GlanceModifier.fillMaxWidth(),
//            style = TextStyle(
//                textAlign = TextAlign.Center
//            )
//        )
//    }
//}
//
//class RefreshAction : ActionCallback {
//    override suspend fun onAction(
//        context: Context,
//        glanceId: GlanceId,
//        parameters: ActionParameters
//    ) {
//        LatestDataRepository.getInstance().update(context)
//    }
//}
//
//fun formatAsPercentage(floatValue: Float): String {
//    return "${String.format("%.0f", floatValue * 100)}%"
//}
//
//fun gauge(percentage: Float): ImageBitmap {
//    val strokeWidth = 15f
//    val width = 140
//    val height = 140
//    val bitmap = ImageBitmap(width, height)
//    val canvas = Canvas(bitmap)
//    val trackPaint = Paint()
//    trackPaint.style = PaintingStyle.Stroke
//    trackPaint.strokeCap = StrokeCap.Round
//    trackPaint.strokeWidth = strokeWidth
//    trackPaint.color = Color.LightGray
//
//    val progressPaint = Paint()
//    progressPaint.style = PaintingStyle.Stroke
//    progressPaint.strokeCap = StrokeCap.Round
//    progressPaint.strokeWidth = strokeWidth
//    progressPaint.color = when (percentage) {
//        in 0f..0.4f -> Red
//        in 0.4f..0.7f -> Sunny
//        else -> Green
//    }
//
//    val rect = Rect(
//        Offset(strokeWidth / 2.0f, strokeWidth / 2.0f),
//        Offset(width.toFloat() - strokeWidth / 2.0f, height.toFloat() - strokeWidth / 2.0f)
//    )
//
//    canvas.drawArc(
//        rect,
//        140f,
//        260f,
//        false,
//        trackPaint
//    )
//
//    canvas.drawArc(
//        rect,
//        140f,
//        percentage * 260f,
//        false,
//        progressPaint
//    )
//
////    val angleInDegrees = (percentage * 260.0) + 50.0
////    val radius = (rect.height / 2)
////    val x = -(radius * sin(Math.toRadians(angleInDegrees))).toFloat() + (rect.width / 2)
////    val y = (radius * cos(Math.toRadians(angleInDegrees))).toFloat() + (rect.height / 2)
////
////    canvas.drawCircle(
////        paint = trackPaint,
////        radius = strokeWidth / 3.0f,
////        center = Offset(x, y)
////    )
//
//    return bitmap
//}
//
//fun drawBattery(): ImageBitmap {
//    val height = 40f
//    val size = Size(height * 1.25f, height)
//    val boxTop = (size.height * 0.11f)
//    val terminalInset = size.width * 0.2f
//    val terminalWidth = size.width * 0.2f
//    val barHeight = 4f
//    val halfBarHeight = barHeight / 2f
//
//    val blackPaint = Paint()
//    blackPaint.color = Color.Black
//    val whitePaint = Paint()
//    whitePaint.color = Color.White
//    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
//    val canvas = Canvas(bitmap)
//
//    fun drawRoundRect(paint: Paint, topLeft: Offset, size: Size, cornerRadius: CornerRadius) {
//        canvas.drawRoundRect(
//            left = topLeft.x,
//            top = topLeft.y,
//            bottom = topLeft.y + size.height,
//            paint = paint,
//            radiusX = cornerRadius.x,
//            radiusY = cornerRadius.y,
//            right = topLeft.x + size.width
//        )
//    }
//
//    // Battery
//    val batterySize = Size(size.width, size.height - boxTop)
//    drawRoundRect(
//        paint = blackPaint,
//        topLeft = Offset(x = 0f, y = boxTop),
//        size = batterySize,
//        cornerRadius = CornerRadius(x = 5f, y = 5f)
//    )
//
//    // Negative terminal
//    drawRoundRect(
//        paint = blackPaint,
//        topLeft = Offset(x = terminalInset, y = 0f),
//        size = Size(terminalWidth, boxTop + barHeight),
//        cornerRadius = CornerRadius(x = 3f, y = 3f)
//    )
//
//    // Positive terminal
//    drawRoundRect(
//        paint = blackPaint,
//        topLeft = Offset(x = size.width - 2 * terminalInset, y = 0f),
//        size = Size(terminalWidth, boxTop + barHeight),
//        cornerRadius = CornerRadius(x = 3f, y = 3f)
//    )
//
//    // Minus
//    drawRoundRect(
//        paint = whitePaint,
//        topLeft = Offset(x = terminalInset, y = boxTop + batterySize.height / 2.0f),
//        size = Size(terminalWidth, barHeight),
//        cornerRadius = CornerRadius(2f, 2f)
//    )
//
//    // Plus
//    drawRoundRect(
//        paint = whitePaint,
//        topLeft = Offset(
//            x = size.width - 2 * terminalInset,
//            y = boxTop + batterySize.height / 2.0f
//        ),
//        size = Size(terminalWidth, barHeight),
//        cornerRadius = CornerRadius(2f, 2f)
//    )
//    drawRoundRect(
//        paint = whitePaint,
//        topLeft = Offset(
//            x = size.width - 2 * terminalInset + (terminalInset / 2f) - halfBarHeight,
//            y = boxTop + batterySize.height / 2.0f - (terminalInset / 2f) + halfBarHeight
//        ),
//        size = Size(barHeight, terminalWidth),
//        cornerRadius = CornerRadius(2f, 2f)
//    )
//
//    return bitmap
//}
//
//
////
////@Composable
////fun ComposeCircularProgressBar(
////    modifier: Modifier = Modifier,
////    percentage: Float,
////    fillColor: Color,
////    backgroundColor: Color,
////    strokeWidth: Dp
////) {
////    Canvas(
////        modifier = modifier
////            .padding(10.dp)
////    ) {
////        // Background Line
////        drawArc(
////            color = backgroundColor,
////            140f,
////            260f,
////            false,
////            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
////            size = Size(size.width, size.height)
////        )
////
////        drawArc(
////            color = fillColor,
////            140f,
////            percentage * 260f,
////            false,
////            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
////            size = Size(size.width, size.height)
////        )
////
////        val angleInDegrees = (percentage * 260.0) + 50.0
////        val radius = (size.height / 2)
////        val x = -(radius * sin(Math.toRadians(angleInDegrees))).toFloat() + (size.width / 2)
////        val y = (radius * cos(Math.toRadians(angleInDegrees))).toFloat() + (size.height / 2)
////
////        drawCircle(
////            color = Color.White,
////            radius = strokeWidth.toPx() / 3.0f,
////            center = Offset(x, y)
////        )
////    }
////}