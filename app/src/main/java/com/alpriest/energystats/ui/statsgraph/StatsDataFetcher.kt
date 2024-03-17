package com.alpriest.energystats.ui.statsgraph

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.ui.summary.ApproximationsCalculator

class StatsDataFetcher(val networking: Networking, val approximationsCalculator: ApproximationsCalculator) {
    suspend fun fetchData(
        device: Device,
        reportVariables: List<ReportVariable>,
        displayMode: StatsDisplayMode
    ): Pair<List<StatsGraphValue>, MutableMap<ReportVariable, Double>> {
        val queryDate = makeQueryDate(displayMode)
        val reportType = makeReportType(displayMode)

        val reportData = networking.fetchReport(
            device.deviceSN,
            variables = reportVariables,
            queryDate = queryDate,
            reportType = reportType
        )

        val totals = approximationsCalculator.generateTotals(device.deviceSN, reportData, reportType, queryDate, reportVariables)

        val updatedData = reportData.flatMap { reportResponse ->
            val reportVariable = ReportVariable.parse(reportResponse.variable)

            return@flatMap reportResponse.values.map { dataPoint ->
                val graphPoint: Int = when (displayMode) {
                    is StatsDisplayMode.Day -> {
                        dataPoint.index - 1
                    }

                    is StatsDisplayMode.Month -> {
                        dataPoint.index
                    }

                    is StatsDisplayMode.Year -> {
                        dataPoint.index
                    }

                    is StatsDisplayMode.Custom -> {
                        dataPoint.index
                    }
                }

                return@map StatsGraphValue(
                    graphPoint = graphPoint,
                    value = dataPoint.value,
                    type = reportVariable
                )
            }
        }

        return Pair(updatedData, totals)
    }

    private fun makeQueryDate(displayMode: StatsDisplayMode): QueryDate {
        return when (displayMode) {
            is StatsDisplayMode.Day -> {
                val date = displayMode.date
                QueryDate(
                    year = date.year,
                    month = date.monthValue,
                    day = date.dayOfMonth
                )
            }

            is StatsDisplayMode.Month -> {
                QueryDate(year = displayMode.year, month = displayMode.month + 1, day = null)
            }

            is StatsDisplayMode.Year -> {
                QueryDate(year = displayMode.year, month = null, day = null)
            }

            is StatsDisplayMode.Custom -> {
                QueryDate.invoke()
            }
        }
    }

    private fun makeReportType(displayMode: StatsDisplayMode): ReportType {
        return when (displayMode) {
            is StatsDisplayMode.Day -> ReportType.day
            is StatsDisplayMode.Month -> ReportType.month
            is StatsDisplayMode.Year -> ReportType.year
            is StatsDisplayMode.Custom -> ReportType.month
        }
    }
}