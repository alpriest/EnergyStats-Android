package com.alpriest.energystats.ui.settings.debug.networkTrace

import android.content.Intent
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.NetworkFacade
import com.alpriest.energystats.services.NetworkService
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.statsgraph.ReportType
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun NetworkTraceDebugView(configManager: ConfigManaging, credentialStore: CredentialStore) {
    val context = LocalContext.current
    val cacheDirectory = File(context.cacheDir, "responses")

    fun variable(variableName: String): Variable? {
        return configManager.variables.firstOrNull { it.variable.lowercase() == variableName.lowercase() }
    }

    suspend fun downloadFiles() {
        val network = NetworkFacade(
            NetworkService(credentialStore, interceptor = ResponseFileInterceptor(cacheDirectory), store = InMemoryLoggingNetworkStore())
        ) { configManager.isDemoUser }

        configManager.currentDevice.value?.let {
            val rawVariables = listOfNotNull(
                variable("feedInPower"),
                variable("gridConsumptionPower"),
                variable("generationPower"),
                variable("loadsPower"),
                variable("pvPower"),
                variable("meterPower2")
            )
            network.fetchRaw(it.deviceID, rawVariables, QueryDate())
            network.fetchReport(it.deviceID, listOf(ReportVariable.Loads, ReportVariable.FeedIn, ReportVariable.GridConsumption), QueryDate(), ReportType.month)
            if (it.battery != null || it.hasBattery) {
                network.fetchBattery(deviceID = it.deviceID)
            }
        }
    }

    val zipFile = kotlin.io.path.createTempFile("comprehensive_debug", ".zip").toFile()
    val zipFileName = FileProvider.getUriForFile(LocalContext.current, "com.alpriest.energystats.ui.statsgraph.ExportFileProvider", zipFile);
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = {
        coroutineScope.launch {
            downloadFiles()
            createZipFile(cacheDirectory, zipFile)

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, zipFileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                type = "text/csv"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
        }
    }) {
        Text("Download zip")
    }
}

fun createZipFile(directory: File, zipFile: File) {
    val zipOutputStream = ZipOutputStream(FileOutputStream(zipFile))
    val files = directory.listFiles() ?: return
    val buffer = ByteArray(1024)

    try {
        for (file in files) {
            // Create a new entry in the ZIP file
            val zipEntry = ZipEntry(file.name)
            zipOutputStream.putNextEntry(zipEntry)

            // Read the file and write it to the ZIP file
            val fileInputStream = FileInputStream(file)
            var length: Int
            while (fileInputStream.read(buffer).also { length = it } > 0) {
                zipOutputStream.write(buffer, 0, length)
            }
            fileInputStream.close()

            // Close the current entry
            zipOutputStream.closeEntry()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        // Close the ZIP file
        zipOutputStream.close()
    }
}

class ResponseFileInterceptor(private val directory: File) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val responseBody = response.body

        if (responseBody != null) {
            val path = request.url.encodedPath
            val fileName = path.substring(path.lastIndexOf("/") + 1)

            val file = File(directory, fileName)

            try {
                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return response
    }
}