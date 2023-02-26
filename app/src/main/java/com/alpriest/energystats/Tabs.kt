package com.alpriest.energystats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.PowerFlowTabView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.SettingsView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(
    configManager: ConfigManaging,
    network: Networking,
    onLogout: () -> Unit,
    userManager: UserManaging,
    themeStream: MutableStateFlow<AppTheme>,
    rawDataStore: RawDataStoring
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val titles = listOf(
        Tab(title = "Energy Flow", icon = Icons.Rounded.SwapVert),
        Tab(title = "Settings", icon = Icons.Rounded.Settings)
    )

    return Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            EnergyStatsTheme {
                HorizontalPager(
                    count = titles.size,
                    state = pagerState
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        when (page) {
                            0 -> PowerFlowTabView(network, configManager, rawDataStore).Content(themeStream = themeStream)
                            1 -> SettingsView(
                                config = configManager,
                                userManager = userManager,
                                onLogout = onLogout,
                                rawDataStore = rawDataStore
                            )
                        }
                    }
                }
            }
        },
        topBar = {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                backgroundColor = MaterialTheme.colors.background,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier
                            .pagerTabIndicatorOffset(pagerState, tabPositions)
                    )
                }
            ) {
                titles.forEachIndexed { index, tabDetail ->
                    Tab(
                        text = {
                            Text(
                                text = tabDetail.title,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Icon(
                                tabDetail.icon,
                                contentDescription = tabDetail.title
                            )
                        },
                        selected = pagerState.currentPage == index,
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = Color.Gray,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(
                                    index
                                )
                            }
                        }
                    )
                }
            }
        }
    )
}

data class Tab(
    val title: String,
    val icon: ImageVector
)