package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.statsgraph.millisToLocalDate
import java.util.Calendar

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
                ParameterGraphVariableChooserView(viewModel)
            }
        }
    }
}

@Composable
fun ParameterGraphVariableChooserView(viewModel: ParametersGraphTabViewModel) {
    Column(
        modifier = Modifier
            .background(Color.White)
    ) {
        Text("Variable list here")
    }
}

@Preview
@Composable
fun ParameterGraphVariableChooserButtonPreview() {
    ParameterGraphVariableChooserButton(viewModel = ParametersGraphTabViewModel(configManager = FakeConfigManager(), networking = DemoNetworking()))
}