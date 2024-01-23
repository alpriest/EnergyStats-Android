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

    fun batteryTimeSet(): HttpUrl {
        return "https://www.foxesscloud.com/c/v0/device/battery/time/set"
            .toHttpUrl()
    }

    fun batteryTimes(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/c/v0/device/battery/time/get"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("sn", deviceSN)
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
            .addQueryParameter("sn", deviceSN)
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
            .addQueryParameter("sn", deviceSN)
            .build()
    }

    fun getOpenBatterySOC(deviceSN: String): HttpUrl {
        return "https://www.foxesscloud.com/op/v0/device/battery/soc/get"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("sn", deviceSN)
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
}

private fun HttpUrl.Builder.addDeviceSN(deviceSN: String): HttpUrl.Builder {
    addQueryParameter("deviceSN", deviceSN)
    return this
}

private fun HttpUrl.Builder.addDeviceID(deviceID: String): HttpUrl.Builder {
    addQueryParameter("deviceID", deviceID)
    return this
}
