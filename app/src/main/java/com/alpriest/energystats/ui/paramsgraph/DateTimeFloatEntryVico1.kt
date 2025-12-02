//package com.alpriest.energystats.ui.paramsgraph
//
//import com.alpriest.energystats.models.Variable
//import com.alpriest.energystats.models.kW
//import com.patrykandpatrick.vico1.core.entry.ChartEntry
//import java.time.LocalDateTime
//
//class DateTimeFloatEntryVico1(
//    val localDateTime: LocalDateTime,
//    override val x: Float,
//    override val y: Float,
//    val type: Variable,
//) : ChartEntry {
//    override fun withY(y: Float): ChartEntry = DateTimeFloatEntryVico1(
//        localDateTime = localDateTime,
//        x = x,
//        y = y,
//        type = type,
//    )
//
//    fun formattedValue(decimalPlaces: Int): String {
//        return when (type.unit) {
//            "kW" -> y.toDouble().kW(decimalPlaces)
//            else -> "$y ${type.unit}"
//        }
//    }
//}
//
//fun DateTimeFloatEntry.toVico1(): DateTimeFloatEntryVico1 =
//    DateTimeFloatEntryVico1(
//        localDateTime = localDateTime,
//        x = x,
//        y = y,
//        type = type
//    )