package com.alpriest.energystats.ui.settings

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun LoadedScaffold(
    title: String,
    navController: NavHostController? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                navigationIcon = {
                    navController?.let {
                        IconButton(onClick = {
                            it.popBackStack()
                        }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, "backIcon")
                        }
                    }
                },
                title = {
                    Text(title)
                }
            )
        },
    ) { _ ->
        content()
    }
}