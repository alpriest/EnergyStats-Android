package com.alpriest.energystats.shared.models

typealias WorkMode = String

object WorkModes {
    const val SelfUse = "SelfUse"
    const val Feedin = "Feedin"
    const val Backup = "Backup"
    const val ForceCharge = "ForceCharge"
    const val ForceDischarge = "ForceDischarge"
    const val PeakShaving = "PeakShaving"
}