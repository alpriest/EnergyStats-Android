package com.alpriest.energystats.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun WelcomeView() {
    Column(
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Yellow.copy(alpha = 0.2f)),
        ) {
            Image(
                painter = painterResource(id = R.drawable.es_icon),
                contentDescription = "ES",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            "Energy management at your fingertips",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 44.dp)
        )

        Button(
            onClick = {},
            modifier = Modifier.padding(top = 44.dp).defaultMinSize(minWidth = 200.dp)
        ) {
            Text("Get started")
        }
    }
}

@Preview(showBackground = true, heightDp = 790, widthDp = 380)
@Composable
fun WelcomeViewPreview() {
    EnergyStatsTheme {
        WelcomeView()
    }
}
