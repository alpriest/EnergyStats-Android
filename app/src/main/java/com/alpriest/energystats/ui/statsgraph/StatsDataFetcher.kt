package com.alpriest.energystats.ui.statsgraph

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.ui.summary.ApproximationsCalculator
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class StatsDataFetcher(val networking: Networking, val approximationsCalculator: ApproximationsCalculator) {
    suspend fun fetchData(
        device: Device,
        reportVariables: List<ReportVariable>,
        displayMode: StatsDisplayMode
    ): Pair<List<StatsGraphValue>, MutableMap<ReportVariable, Double>> {
        val queryDate = makeQueryDate(displayMode)
        val reportType = makeReportType(displayMode)

        val reports = networking.fetchReport(
            device.deviceSN,
            variables = reportVariables,
            queryDate = queryDate,
            reportType = reportType
        )

        val totals = approximationsCalculator.generateTotals(device.deviceSN, reports, reportType, queryDate, reportVariables)

        val updatedData = reports.flatMap { reportResponse ->
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
                    type = reportVariable,
                    graphPoint = graphPoint,
                    graphValue = dataPoint.value
                )
            }
        }

        return Pair(updatedData, totals)
    }

    suspend fun fetchCustomData(
        device: Device,
        start: LocalDate,
        end: LocalDate,
        reportVariables: List<ReportVariable>
    ): Pair<List<StatsGraphValue>, MutableMap<ReportVariable, Double>> {
        var current = start
        val accumulatedGraphValues: MutableList<StatsGraphValue> = mutableListOf()
        var accumulatedReportResponses: MutableList<OpenReportResponse> = mutableListOf()

        while (current.year < end.year || (current.year == end.year && current.month <= end.month)) {
            val month: Int = current.monthValue
            val year: Int = current.year
            val queryDate = QueryDate(year = year, month = month, day = null)
            val reports = networking.fetchReport(
                device.deviceSN,
                variables = reportVariables,
                queryDate = queryDate,
                reportType = ReportType.month
            ).map { response ->
                response.copy(values = response.values.filter {
                    val dataDate: LocalDate = LocalDate.of(year, month, it.index)

                    start.atStartOfDay() <= dataDate.atStartOfDay() && dataDate.atStartOfDay() <= end.atStartOfDay()
                })
            }

            val graphValues = reports.flatMap { reportResponse ->
                val reportVariable = ReportVariable.parse(reportResponse.variable)

                reportResponse.values.map { dataPoint ->
                    val index = ChronoUnit.DAYS.between(start.atStartOfDay().toLocalDate(), LocalDate.of(year, month, dataPoint.index)).toInt()

                    StatsGraphValue(
                        type = reportVariable,
                        graphPoint = index,
                        graphValue = dataPoint.value
                    )
                }
            }

            reports.forEach { response ->
                if (accumulatedReportResponses.find { it.variable == response.variable } != null) {
                    accumulatedReportResponses = accumulatedReportResponses.map {
                        if (it.variable == response.variable) {
                            OpenReportResponse(variable = it.variable, unit = it.unit, values = it.values + response.values)
                        } else {
                            it
                        }
                    }.toMutableList()
                } else {
                    accumulatedReportResponses.add(response)
                }
            }

            accumulatedGraphValues.addAll(graphValues)

            current = current.plusMonths(1)
        }

        val totals = approximationsCalculator.generateTotals(device.deviceSN, accumulatedReportResponses, ReportType.month, queryDate = null, reportVariables)

        return Pair(accumulatedGraphValues, totals)
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