package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ParameterGraphVariableChooserButton(viewModel: ParametersGraphTabViewModel) {
    var showing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.BottomCenter)) {
        Button(
            onClick = { showing = true },
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Checklist,
                contentDescription = null
            )
        }
        if (showing) {
            Dialog(
                onDismissRequest = { showing = false },
            ) {
                ParameterGraphVariableChooserView(ParameterGraphVariableChooserViewModel(viewModel.graphVariablesStream))
            }
        }
    }
}