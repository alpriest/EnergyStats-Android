package com.alpriest.energystats.shared.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object URLs {
    fun getErrorMessages(): HttpUrl {
        return "https://www.foxesscloud.com/c/v0/errors/message"
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

    fun getOpenVariables(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/variable/get"
            .toHttpUrl()
            .newBuilder()
            .build()
    }

    fun getOpenRealData(): HttpUrl {
        return "https://www.foxesscloud.com/op/v1/device/real/query"
            .toHttpUrl()
    }

    fun getOpenHistoryData(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/history/query"
            .toHttpUrl()
    }

    fun getOpenDeviceList(): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/list"
            .toHttpUrl()
    }

    fun getOpenDeviceDetail(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/op/v1/device/detail"
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
