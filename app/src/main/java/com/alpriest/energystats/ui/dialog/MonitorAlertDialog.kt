package com.alpriest.energystats.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.alpriest.energystats.services.UnacceptableException
import com.alpriest.energystats.ui.helpers.UnsupportedErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.ui.theme.ESButton

data class MonitorAlertDialogData(
    val ex: Exception?,
    val message: String?
)

@Composable
fun MonitorAlertDialog(viewModel: AlertDialogMessageProviding, userManager: UserManaging) {
    val message = viewModel.alertDialogMessage.collectAsState().value

    message?.let {
        when (it.ex) {
            is UnacceptableException -> {
                UnsupportedErrorView(onDismiss = {
                    viewModel.resetDialogMessage()
                })
            }

            else -> {
                AlertDialog(message = it.message ?: "Unknown error", onDismiss = {
                    viewModel.resetDialogMessage()
                })
            }
        }
    }
}

@Composable
fun AlertDialog(message: String, onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(vertical = 24.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(message)
                Spacer(modifier = Modifier.height(8.dp))
                ESButton(onClick = onDismiss) {
                    Text(context.getString(R.string.ok))
                }
            }
        }
    }
}
