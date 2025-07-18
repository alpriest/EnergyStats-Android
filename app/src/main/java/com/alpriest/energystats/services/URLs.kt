package com.alpriest.energystats.services

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object URLs {
    fun getErrorMessages(): HttpUrl {
        return "https://www.foxesscloud.com/c/v0/errors/message"
            .toHttpUrl()
    }

    fun login(): HttpUrl {
        return "https://www.foxesscloud.com/c/v0/user/login"
            .toHttpUrl()
    }

    fun getOpenModuleList(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/module/list"
            .toHttpUrl()
    }

    fun setOpenBatteryChargeTimes(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/battery/forceChargeTime/set"
            .toHttpUrl()
    }

    fun getOpenBatteryChargeTimes(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/battery/forceChargeTime/get"
            .toHttpUrl()
            .newBuilder()
            .addSN(deviceSN)
            .build()
    }

    fun socSet(): HttpUrl {
        return "https://www.foxesscloud.com/c/v0/device/battery/soc/set"
            .toHttpUrl()
    }

    fun getOpenVariables(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/variable/get"
            .toHttpUrl()
            .newBuilder()
            .build()
    }

    fun raw(): HttpUrl {
        return "https://www.foxesscloud.com/c/v0/device/history/raw"
            .toHttpUrl()
    }

    fun socGet(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/c/v0/device/battery/soc/get"
            .toHttpUrl()
            .newBuilder()
            .addSN(deviceSN)
            .build()
    }

    fun getOpenRealData(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/real/query"
            .toHttpUrl()
    }

    fun getOpenHistoryData(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/history/query"
            .toHttpUrl()
    }

    fun getSchedulerFlag(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/get/flag"
            .toHttpUrl()
            .newBuilder()
            .addDeviceSN(deviceSN)
            .build()
    }

    fun getDeleteSchedule(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/disable"
            .toHttpUrl()
            .newBuilder()
            .addDeviceSN(deviceSN)
            .build()
    }

    fun getSchedule(deviceSN: String, templateID: String): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/detail"
            .toHttpUrl()
            .newBuilder()
            .addDeviceSN(deviceSN)
            .addQueryParameter("templateID", templateID)
            .build()
    }

    fun deleteScheduleTemplate(templateID: String): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/delete"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("templateID", templateID)
            .build()
    }

    fun enableSchedule(): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/enable"
            .toHttpUrl()
    }

    fun schedulerModes(deviceID: String): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/modes/get"
            .toHttpUrl()
            .newBuilder()
            .addDeviceID(deviceID)
            .build()
    }

    fun getCurrentSchedule(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/list"
            .toHttpUrl()
            .newBuilder()
            .addDeviceSN(deviceSN)
            .build()
    }

    fun createScheduleTemplate(): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/create".toHttpUrl()
    }

    fun fetchScheduleTemplates(): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/edit/list?templateType=2".toHttpUrl()
    }

    fun saveScheduleTemplate(): HttpUrl {
        return "https://www.foxesscloud.com/generic/v0/device/scheduler/save".toHttpUrl()
    }

    fun getOpenDeviceList(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/list"
            .toHttpUrl()
    }

    fun getOpenDeviceDetail(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/detail"
            .toHttpUrl()
            .newBuilder()
            .addSN(deviceSN)
            .build()
    }

    fun getOpenBatterySOC(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/battery/soc/get"
            .toHttpUrl()
            .newBuilder()
            .addSN(deviceSN)
            .build()
    }

    fun getOpenReportData(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/report/query"
            .toHttpUrl()
    }

    fun setOpenBatterySOC(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/battery/soc/set"
            .toHttpUrl()
    }

    fun getOpenSchedulerFlag(): HttpUrl {
        return "https://www.foxesscloud.com/op/v1/device/scheduler/get/flag"
            .toHttpUrl()
    }

    fun getOpenCurrentSchedule(): HttpUrl {
        return "https://www.foxesscloud.com/op/v1/device/scheduler/get"
            .toHttpUrl()
    }

    fun setOpenSchedulerFlag(): HttpUrl {
        return "https://www.foxesscloud.com/op/v1/device/scheduler/set/flag"
            .toHttpUrl()
    }

    fun setOpenCurrentSchedule(): HttpUrl {
        return "https://www.foxesscloud.com/op/v1/device/scheduler/enable"
            .toHttpUrl()
    }

    fun getOpenPlantList(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/plant/list"
            .toHttpUrl()
    }

    fun getOpenPlantDetail(stationID: String): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/plant/detail"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("id", stationID)
            .build()
    }

    fun getRequestCount(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/user/getAccessCount".toHttpUrl()
    }

    fun fetchDeviceSettingsItem(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/setting/get".toHttpUrl()
    }

    fun setDeviceSettingsItem(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/setting/set".toHttpUrl()
    }

    fun getDevicePeakShavingSettings(): HttpUrl = "https://www.foxesscloud.com/op/v0/device/peakShaving/get".toHttpUrl()
    fun setDevicePeakShavingSettings(): HttpUrl = "https://www.foxesscloud.com/op/v0/device/peakShaving/set".toHttpUrl()

    fun fetchPowerGeneration(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/generation"
            .toHttpUrl()
            .newBuilder()
            .addSN(deviceSN)
            .build()
    }
}

private fun HttpUrl.Builder.addSN(deviceSN: String): HttpUrl.Builder {
    addQueryParameter("sn", deviceSN)
    return this
}

private fun HttpUrl.Builder.addDeviceSN(deviceSN: String): HttpUrl.Builder {
    addQueryParameter("deviceSN", deviceSN)
    return this
}

private fun HttpUrl.Builder.addDeviceID(deviceID: String): HttpUrl.Builder {
    addQueryParameter("deviceID", deviceID)
    return this
}
