package com.alpriest.energystats.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding

@Composable
fun MonitorAlertDialog(viewModel: AlertDialogMessageProviding) {
    val message = viewModel.alertDialogMessage.collectAsState().value

    message?.let {
        AlertDialog(message = it, onDismiss = { viewModel.alertDialogMessage.value = null })
    }
}

@Composable
private fun AlertDialog(message: String, onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(message)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss) {
                    Text(context.getString(R.string.ok))
                }
            }
        }
    }
}
