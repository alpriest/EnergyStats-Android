package com.alpriest.energystats.ui.paramsgraph

import android.content.Context
import android.net.Uri
import java.time.LocalDateTime

interface ExportProviding {
    var exportFileUri: Uri?
    fun exportTo(context: Context, uri: Uri)
}

data class LastLoadState<T>(
    val lastLoadTime: LocalDateTime,
    val loadState: T
)
