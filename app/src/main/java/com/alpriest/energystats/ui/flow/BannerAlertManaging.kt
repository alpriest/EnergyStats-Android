package com.alpriest.energystats.ui.flow

import kotlinx.coroutines.flow.MutableStateFlow

interface BannerAlertManaging {
    var bannerAlertStream: MutableStateFlow<BannerAlertType?>
    fun clearDeviceBanner()
    fun deviceIsOffline()
    fun showToast(message: String?)
}

sealed class BannerAlertType {
    data object Offline : BannerAlertType()
    data class Toast(val message: String) : BannerAlertType()
}

class BannerAlertManager : BannerAlertManaging {
    override var bannerAlertStream = MutableStateFlow<BannerAlertType?>(null)
    private var seenOfflineBanner = false

    override fun deviceIsOffline() {
        if (!seenOfflineBanner) {
            seenOfflineBanner = true
            bannerAlertStream.value = BannerAlertType.Offline
        }
    }

    override fun clearDeviceBanner() {
        bannerAlertStream.value = null
    }

    override fun showToast(message: String?) {
        bannerAlertStream.value = BannerAlertType.Toast(message ?: "Something went wrong. Please try again.")
    }
}