package com.alpriest.energystats.helpers

import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import kotlinx.coroutines.tasks.await

object WearableApiAvailability {
    suspend fun isAvailable(api: GoogleApi<*>): Boolean {
        return try {
            GoogleApiAvailability.getInstance()
                .checkApiAvailability(api)
                .await()

            true
        } catch (e: AvailabilityException) {
            Log.d(
                TAG,
                "${api.javaClass.simpleName} API is not available in this device.",
            )
            false
        }
    }

    val TAG = "WearableApiAvailability"
}