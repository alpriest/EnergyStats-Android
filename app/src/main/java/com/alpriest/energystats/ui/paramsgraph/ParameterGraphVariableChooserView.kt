package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking

class ParameterGraphVariableChooserViewModel {

}

@Composable
fun ParameterGraphVariableChooserView(viewModel: ParameterGraphVariableChooserViewModel) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.BottomCenter)
    ) {
        Column(
            Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .offset(y = -44.dp)
        ) {
            Column(
                modifier = Modifier

            ) {
                Text("Predefined selections")

                OutlinedButton(onClick = { /*TODO*/ }) { Text("Default") }
                OutlinedButton(onClick = { /*TODO*/ }) { Text("Compare strings") }
                OutlinedButton(onClick = { /*TODO*/ }) { Text("Temperatures") }
                OutlinedButton(onClick = { /*TODO*/ }) { Text("None") }
            }

            Column() {
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
                Text("One")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .padding(12.dp)
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { /*TODO*/ }) {
                    Text("Cancel")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { /*TODO*/ }
                ) {
                    Text("Apply")
                }
            }
            Text("Note that not all parameters contain values", modifier = Modifier.align(CenterHorizontally))
        }
    }
}

@Preview(widthDp = 400, heightDp = 400)
@Composable
fun ParameterGraphVariableChooserViewPreview() {
    ParameterGraphVariableChooserView(viewModel = ParameterGraphVariableChooserViewModel())
}