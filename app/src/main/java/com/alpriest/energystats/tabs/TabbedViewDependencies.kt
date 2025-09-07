package com.alpriest.energystats.tabs

import android.net.Uri
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.WidgetDataSharing
import com.alpriest.energystats.ui.flow.BannerAlertManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStoring
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

class TabbedViewDependencies(
    val configManager: ConfigManaging,
    val network: Networking,
    val userManager: UserManaging,
    val onLogout: () -> Unit,
    val themeStream: MutableStateFlow<AppTheme>,
    val onRateApp: () -> Unit,
    val onBuyMeCoffee: () -> Unit,
    val onWriteTempFile: (String, String) -> Uri?,
    val filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    val solarForecastingProvider: () -> SolcastCaching,
    val widgetDataSharer: WidgetDataSharing,
    val bannerAlertManager: BannerAlertManaging,
    val templateStore: TemplateStoring
)