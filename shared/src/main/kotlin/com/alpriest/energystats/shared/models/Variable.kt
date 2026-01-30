package com.alpriest.energystats.shared.models

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.shared.helpers.md5
import kotlinx.coroutines.flow.StateFlow

class Variable(
    val name: String,
    val variable: String,
    val unit: String
) {
    @Composable
    fun colour(appSettings: StateFlow<AppSettings>): Color {
        return when (this.variable) {
            "generationPower" -> Color(244, 184, 96)
            "feedinPower" -> Color(181, 121, 223)
            "batChargePower" -> Color(125, 208, 130)
            "batDischargePower" -> Color(80, 147, 248)
            "gridConsumptionPower" -> Color(236, 109, 96)
            "loadsPower" -> Color(209, 207, 83)
            Variable.solcastPrediction.variable -> if (isDarkMode(appSettings)) Color.White else Color.Black
            else ->
                this.variable.md5().let {
                    Color(("#" + it.subSequence(0, 6).toString()).toColorInt())
                }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Variable

        return variable == other.variable
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + variable.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }

    fun fuzzyNameMatches(other: String): Boolean {
        // Systems with multiple batteries can return the raw variable name with a numbering appended
        return variable == other ||
                "${variable}_1" == other ||
                "${variable}_2" == other ||
                "${variable}_3" == other
    }

    companion object
}

val Variable.Companion.solcastPrediction: Variable
    get() = Variable(
        name = "Solcast solar prediction",
        variable = "solcast_prediction",
        unit = "kW"
    )

@Composable
fun isDarkMode(appSettingsStream: StateFlow<AppSettings>): Boolean {
    return isDarkMode(colorTheme = appSettingsStream.collectAsStateWithLifecycle().value.colorTheme)
}

@Composable
fun isDarkMode(colorTheme: ColorThemeMode): Boolean {
    return when (colorTheme) {
        ColorThemeMode.Light -> false
        ColorThemeMode.Dark -> true
        ColorThemeMode.Auto -> isSystemInDarkTheme()
    }
}
