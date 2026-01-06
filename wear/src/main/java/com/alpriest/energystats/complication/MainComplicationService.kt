package com.alpriest.energystats.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.alpriest.energystats.shared.helpers.asPercent
import com.alpriest.energystats.sync.SharedPreferencesConfigStore
import com.alpriest.energystats.sync.make

class MainComplicationService : SuspendingComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) {
            return null
        }
        return createComplicationData("55%", "Home Battery Level")
    }

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        super.onComplicationActivated(complicationInstanceId, type)

        // Force an immediate refresh when the complication is (re)bound in the watch face.
        ComplicationDataSourceUpdateRequester
            .create(this, ComponentName(this, MainComplicationService::class.java))
            .requestUpdate(complicationInstanceId)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val configStore = SharedPreferencesConfigStore.make(this)
        return createComplicationData(configStore.batteryChargeLevel.asPercent(), configStore.batteryChargeLevel.asPercent() + " battery charge level")
    }

    private fun createComplicationData(text: String, contentDescription: String) =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        ).build()

    companion object {
        fun requestRefresh(applicationContext: Context) {
            ComplicationDataSourceUpdateRequester
                .create(applicationContext, ComponentName(applicationContext, MainComplicationService::class.java))
                .requestUpdateAll()
        }
    }
}
