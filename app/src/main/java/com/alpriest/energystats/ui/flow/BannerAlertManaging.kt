package com.alpriest.energystats.ui.flow

import kotlinx.coroutines.flow.MutableStateFlow

interface BannerAlertManaging {
    var bannerAlertStream: MutableStateFlow<BannerAlertType?>
    fun clearDeviceBanner()
    fun deviceIsOffline()
}

enum class BannerAlertType {
    Offline
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
}