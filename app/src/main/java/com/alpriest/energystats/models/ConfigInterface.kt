package com.alpriest.energystats.models

interface ConfigInterface {
    var minSOC: String?
    var batteryCapacity: String?
    var deviceID: String?
    var deviceSN: String?
    var hasBattery: Boolean
    var hasPV: Boolean
    var isDemoUser: Boolean
}