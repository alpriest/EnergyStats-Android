package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadedScaffold(
    title: String,
    navController: NavHostController? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (modifier: Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary
                ),
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
                },
                actions = actions
            )
        },
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}