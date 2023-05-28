package com.alpriest.energystats.models

import java.math.BigInteger
import java.security.MessageDigest

fun String.md5(): String? {
    val md5Digest = MessageDigest.getInstance("MD5")
    val bytes = this.toByteArray(Charsets.UTF_8)
    val digestBytes = md5Digest.digest(bytes)

    val bigInt = BigInteger(1, digestBytes)
    val md5Hash = bigInt.toString(16)

    // Pad with leading zeros if necessary
    return md5Hash.padStart(32, '0')
}