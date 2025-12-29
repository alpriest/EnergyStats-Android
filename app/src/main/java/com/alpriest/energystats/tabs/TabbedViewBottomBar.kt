package com.alpriest.energystats.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.settings.darkenedBackground
import com.alpriest.energystats.ui.settings.darkenedBackgroundColor
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.launch

@Composable
fun TabbedViewBottomBar(pagerState: PagerState, titles: List<TitleItem>, dependencies: TabbedViewDependencies) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .darkenedBackground()
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = darkenedBackgroundColor(),
            divider = {},
            modifier = Modifier.windowInsetsPadding(WindowInsets.Companion.navigationBars)
        ) {
            titles.forEachIndexed { index, item ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.scrollToPage(
                                index
                            )
                        }
                    },
                    content = {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(imageVector = item.icon, contentDescription = null)
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (dependencies.configManager.isDemoUser && item.isSettings) {
                                DemoOverlay()
                            }
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = DimmedTextColor,
                )
            }
        }
    }
}