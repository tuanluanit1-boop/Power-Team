package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

enum class NavigationTab(
    val labelVi: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Trang chủ", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_home"),
    REFERRALS("Cơ hội", Icons.Filled.SwapCalls, Icons.Outlined.SwapCalls, "tab_referrals"),
    MEETINGS_121("1-2-1", Icons.Filled.PeopleAlt, Icons.Outlined.PeopleAlt, "tab_121"),
    FINANCE("Ví & Quỹ", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "tab_finance"),
    PROFILE("Quản trị", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, "tab_admin")
}

@Composable
fun BottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    activeRole: UserRole,
    referralsBadgeCount: Int = 0,
    penaltiesBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Navy700,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier.testTag("bottom_nav_bar")
    ) {
        NavigationTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            val displayLabel = if (tab == NavigationTab.PROFILE) {
                if (activeRole == UserRole.ADMIN) "Quản trị" else "Hồ sơ"
            } else {
                tab.labelVi
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab == NavigationTab.REFERRALS && referralsBadgeCount > 0) {
                                Badge(containerColor = HotRed) {
                                    Text(
                                        text = referralsBadgeCount.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            } else if (tab == NavigationTab.FINANCE && penaltiesBadgeCount > 0) {
                                Badge(containerColor = Orange500) {
                                    Text(
                                        text = "!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = displayLabel,
                            tint = if (isSelected) Orange500 else SlateTextSecondary
                        )
                    }
                },
                label = {
                    Text(
                        text = displayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Orange600 else SlateTextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Orange100,
                    selectedIconColor = Orange600,
                    unselectedIconColor = SlateTextSecondary,
                    selectedTextColor = Orange600,
                    unselectedTextColor = SlateTextSecondary
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}
