package com.alpriest.energystats.ui.settings.readonly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging

@Composable
fun ProtectedContent(
    configManager: ConfigManaging,
    modifier: Modifier = Modifier,
    protected: @Composable () -> Unit = { DefaultReadOnlyBlocked(modifier) },
    content: @Composable () -> Unit
) {
    if (configManager.isReadOnly) {
        protected()
    } else {
        content()
    }
}

@Composable
private fun DefaultReadOnlyBlocked(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Block,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text("This functionality is not available in read-only mode")
    }
}

@Preview(showBackground = true)
@Composable
fun ProtectedContentPreview() {
    ProtectedContent(configManager = FakeConfigManager.make({ it.isReadOnly = true })) {
        Text("sdfs")
    }
}