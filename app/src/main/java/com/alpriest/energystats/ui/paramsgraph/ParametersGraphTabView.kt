package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ParametersGraphTabView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>) {
    Column {
        ParameterGraphHeaderView()
    }
}

@Composable
fun ParameterGraphHeaderView() {
    var hours by remember { mutableStateOf(0) }
    var showingVariables by remember { mutableStateOf(false) }

    Row {
        IconButton(onClick = { showingVariables = !showingVariables }) {
            Icon(
                imageVector = Icons.Filled.Checklist,
                contentDescription = "Visibility",
            )
        }
    }
}

class ParametersGraphTabViewModel : ViewModel() {

}

@Preview
@Composable
fun PreviewParameterGraphHeaderView() {

    ParameterGraphHeaderView()
}