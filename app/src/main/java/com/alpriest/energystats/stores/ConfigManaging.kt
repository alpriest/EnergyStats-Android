package com.alpriest.energystats.stores

interface ConfigManaging {
    val minSOC: Double
    val batteryCapacityW: Int
    val deviceSN: String?
    val deviceID: String?
    val hasPV: Boolean
    var hasBattery: Boolean
    var isDemoUser: Boolean
    fun logout()
    suspend fun findDevice()
}