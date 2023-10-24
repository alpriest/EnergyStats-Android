package com.alpriest.energystats.ui.statsgraph

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.power
import com.alpriest.energystats.ui.CalculationBreakdown
import com.alpriest.energystats.ui.flow.EnergyStatsFinancialModel
import com.alpriest.energystats.ui.flow.TotalsViewModel
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.ApproximationHeaderText
import com.alpriest.energystats.ui.theme.DarkApproximationBackground
import com.alpriest.energystats.ui.theme.DarkApproximationHeader
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.LightApproximationBackground
import com.alpriest.energystats.ui.theme.LightApproximationHeader
import kotlinx.coroutines.flow.MutableStateFlow

data class ApproximationsViewModel(
    val netSelfSufficiencyEstimate: String?,
    val netSelfSufficiencyEstimateCalculationBreakdown: CalculationBreakdown,
    val absoluteSelfSufficiencyEstimate: String?,
    val absoluteSelfSufficiencyEstimateCalculationBreakdown: CalculationBreakdown,
    val financialModel: EnergyStatsFinancialModel?,
    val homeUsage: Double?,
    val totalsViewModel: TotalsViewModel?
)

@Composable
fun CalculationBreakdownView(visible: Boolean, calculationBreakdown: CalculationBreakdown, fontSize: TextUnit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            Modifier
                .padding(bottom = 8.dp)
                .background(
                    colors.ApproximationBackground,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = ApproximationHeader,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            Text(
                calculationBreakdown.calculation,
                fontSize = fontSize
            )
            Text(
                calculationBreakdown.formula,
                fontSize = fontSize
            )
        }
    }
}

@Composable
fun ApproximationView(themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier, viewModel: ApproximationsViewModel) {
    val appTheme = themeStream.collectAsState().value
    val fontSize = appTheme.fontSize()
    val selfSufficiency = when (appTheme.selfSufficiencyEstimateMode) {
        SelfSufficiencyEstimateMode.Off -> null
        SelfSufficiencyEstimateMode.Net -> viewModel.netSelfSufficiencyEstimate
        SelfSufficiencyEstimateMode.Absolute -> viewModel.absoluteSelfSufficiencyEstimate
    }
    val selfSufficiencyCalculations = when (appTheme.selfSufficiencyEstimateMode) {
        SelfSufficiencyEstimateMode.Off -> null
        SelfSufficiencyEstimateMode.Net -> viewModel.netSelfSufficiencyEstimateCalculationBreakdown
        SelfSufficiencyEstimateMode.Absolute -> viewModel.absoluteSelfSufficiencyEstimateCalculationBreakdown
    }
    var showingApproximations by remember { mutableStateOf(true) }

    Box(modifier) {
        Column(
            Modifier
                .background(
                    colors.ApproximationBackground,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = ApproximationHeader,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                if (appTheme.selfSufficiencyEstimateMode != SelfSufficiencyEstimateMode.Off && selfSufficiency != null) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.self_sufficiency),
                            fontSize = fontSize
                        )
                        Text(
                            selfSufficiency,
                            fontSize = fontSize
                        )
                    }

                    selfSufficiencyCalculations?.let {
                        CalculationBreakdownView(showingApproximations, it, fontSize)
                    }
                }

                viewModel.homeUsage?.let {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.home_usage),
                            fontSize = fontSize
                        )
                        Text(
                            it.power(appTheme.displayUnit, appTheme.decimalPlaces),
                            fontSize = fontSize
                        )
                    }
                }

                viewModel.totalsViewModel?.solar?.let {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Solar generated",
                            fontSize = fontSize
                        )
                        Text(
                            it.power(appTheme.displayUnit, appTheme.decimalPlaces),
                            fontSize = fontSize
                        )
                    }
                }

                viewModel.financialModel?.let {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Export income",
                            fontSize = fontSize
                        )
                        Text(
                            it.exportIncome.formattedAmount(),
                            fontSize = fontSize
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Grid import avoided",
                            fontSize = fontSize
                        )
                        Text(
                            it.solarSaving.formattedAmount(),
                            fontSize = fontSize
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total benefit",
                            fontSize = fontSize
                        )
                        Text(
                            it.total.formattedAmount(),
                            fontSize = fontSize
                        )
                    }
                }
            }
        }

        Text(
            stringResource(R.string.approximations),
            Modifier
                .offset(x = 8.dp, y = (-11).dp)
                .background(
                    ApproximationHeader,
                    shape = RoundedCornerShape(size = 4.dp)
                )
                .padding(horizontal = 2.dp, vertical = 1.dp),
            color = ApproximationHeaderText,
        )

        Icon(
            imageVector = if (showingApproximations) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = null,
            tint = ApproximationHeaderText,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = (-11).dp)
                .height(21.dp)
                .background(
                    ApproximationHeader,
                    shape = RoundedCornerShape(size = 4.dp)
                )
                .padding(horizontal = 2.dp, vertical = 1.dp)
                .clickable { showingApproximations = !showingApproximations }
        )
    }
}

@Preview
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StatsApproximationViewPreview() {
    EnergyStatsTheme() {
        ApproximationView(
            themeStream = MutableStateFlow(AppTheme.preview().copy(selfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.Absolute)),
            viewModel = ApproximationsViewModel(
                netSelfSufficiencyEstimate = "95%",
                netSelfSufficiencyEstimateCalculationBreakdown = CalculationBreakdown("abc", "def"),
                absoluteSelfSufficiencyEstimate = "100%",
                absoluteSelfSufficiencyEstimateCalculationBreakdown = CalculationBreakdown("abc", "def"),
                financialModel = null,
                homeUsage = 4.5,
                totalsViewModel = TotalsViewModel(
                    grid = 1.0,
                    feedIn = 2.0,
                    loads = 5.0,
                    batteryCharge = 2.3,
                    batteryDischarge = 1.2
                )
            ),
            modifier = Modifier.padding(24.dp)
        )
    }
}

val ApproximationHeader: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkApproximationHeader else LightApproximationHeader

val Colors.ApproximationBackground: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkApproximationBackground else LightApproximationBackground
