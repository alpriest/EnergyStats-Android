package com.alpriest.energystats.models

data class AddressBookResponse(
    val softVersion: SoftwareVersion
)

data class SoftwareVersion(
    val master: String,
    val slave: String,
    val manager: String
)
