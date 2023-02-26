package com.alpriest.energystats.models

interface ConfigInterface {
    var useLargeDisplay: Boolean
    var minSOC: String?
    var batteryCapacityW: String?
    var deviceID: String?
    var deviceSN: String?
    var hasBattery: Boolean
    var hasPV: Boolean
    var isDemoUser: Boolean
}