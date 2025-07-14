package com.alpriest.energystats.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.alpriest.energystats.MainActivity
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.stores.WidgetTapAction
import com.alpriest.energystats.ui.theme.Sunny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GenerationStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GenerationStatsWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                GenerationStatsDataRepository.getInstance().update(context)
            } catch (_: Exception) {
            }
        }
    }
}

class GenerationStatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = GenerationStatsDataRepository.getInstance()
        repository.updateFromSharedConfig(context)

        provideContent {
            val launchIntent = Intent(LocalContext.current, MainActivity::class.java)
            val action: Action = when (repository.tapAction) {
                WidgetTapAction.Launch -> actionStartActivity(launchIntent)
                WidgetTapAction.Refresh -> actionRunCallback<GenerationStatsRefreshAction>()
            }
            GenerationStatsWidgetContent(repository.today, repository.month, repository.cumulative, action)
        }
    }
}

private fun createGradientBitmap(
    width: Int,
    height: Int,
    startColor: Int,
    endColor: Int
): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    val gradient = LinearGradient(
        -100f, -50f, width.toFloat(), height.toFloat(),
        startColor,
        endColor,
        Shader.TileMode.CLAMP
    )

    paint.shader = gradient
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    return bitmap
}

@Composable
fun GenerationStatsWidgetContent(
    today: Double,
    month: Double,
    cumulative: Double,
    tapAction: Action,
    color: ColorProvider = GlanceTheme.colors.onSurface
) {
    val labelStyle = TextStyle(
        textAlign = TextAlign.Start,
        fontSize = 12.sp,
        color = color
    )
    val numberStyle = TextStyle(
        textAlign = TextAlign.End,
        fontSize = 16.sp,
        color = color
    )
    val gradientBitmap = createGradientBitmap(140, 40, Sunny.toArgb(), 0xFFFFFFFF.toInt())

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = tapAction)
            .background(ImageProvider(gradientBitmap))
            .padding(12.dp)
    ) {
        Spacer(modifier = GlanceModifier.defaultWeight())

        Row(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Text(
                "Today",
                style = labelStyle
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                today.kW(1),
                style = numberStyle
            )
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Text(
                "Month",
                style = labelStyle
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                month.kW(1),
                style = numberStyle
            )
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Text(
                "Lifetime",
                style = labelStyle
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                cumulative.kW(1),
                style = numberStyle
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())
    }
}

class GenerationStatsRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        GenerationStatsDataRepository.getInstance().update(context)
    }
}
