package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class ScheduleTemplateListViewModel(
    private val config: ConfigManaging,
    private val templateStore: TemplateStoring,
    private val navController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val templateStream = MutableStateFlow<List<ScheduleTemplate>>(listOf())

    fun load(context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        config.currentDevice.value?.let { _ ->
            templateStream.value = templateStore.load()
        }
    }

    fun createTemplate(templateName: String, context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        templateStore.create(templateName)
        load(context)
    }

    fun edit(template: ScheduleTemplate) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        EditScheduleStore.shared.reset()
        EditScheduleStore.shared.templateStream.value = template
        EditScheduleStore.shared.allowDeletion = true

        navController.navigate(SettingsScreen.EditTemplate.name)
    }

    fun exportTo(uri: Uri, context: Context) {
        // Attempt to persist write permission if available (SAF URIs)
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Not persistable or not granted; continue with best-effort write.
            }
        }

        try {
            val exportPayload = ExportedTemplateList(
                version = 1,
                templates = templateStream.value
            )

            val json = Gson().toJson(exportPayload)

            // Prefer the "w" mode if supported to truncate/overwrite; fallback to default
            val out = context.contentResolver.openOutputStream(uri, "w")
                ?: context.contentResolver.openOutputStream(uri)
                ?: throw IllegalArgumentException("Could not open file for writing")

            out.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            alertDialogMessage.value = MonitorAlertDialogData(
                ex = null,
                message = "Exported ${templateStream.value.size} template(s)."
            )
        } catch (t: Throwable) {
            alertDialogMessage.value = MonitorAlertDialogData(
                ex = null,
                message = "Could not export templates: ${t.message ?: "Unknown error"}"
            )
        }
    }

    fun importFrom(uri: Uri, context: Context, replaceExistingTemplates: Boolean) {
        // Request persistable read permission if it's a SAF content Uri
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Not persistable, or permission not granted by the picker — safe to ignore.
            }
        }

        try {
            // Read file contents
            val json = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: throw IllegalArgumentException("Could not open selected file")

            // Decode JSON with Gson (unknown fields are ignored by default)
            val imported = Gson().fromJson(json, ExportedTemplateList::class.java)

            // Re-ID to avoid collisions
            val remapped = imported.templates.map { template ->
                template.copy(id = UUID.randomUUID().toString())
            }

            // Update in-memory list
            if (replaceExistingTemplates) {
                templateStream.value.forEach { templateStore.delete(it) }
                templateStream.value = remapped
            } else {
                templateStream.value = templateStream.value + remapped
            }

            // Persist
            remapped.forEach { templateStore.save(it) }
        } catch (t: Throwable) {
            // Surface an error via your existing alert dialog mechanism
            alertDialogMessage.value = MonitorAlertDialogData(
                ex = null,
                message = "Could not import templates: ${t.message ?: "Unknown error"}"
            )
        }
    }
}

private data class ExportedTemplateList(
    val version: Int,
    val templates: List<ScheduleTemplate>
)
