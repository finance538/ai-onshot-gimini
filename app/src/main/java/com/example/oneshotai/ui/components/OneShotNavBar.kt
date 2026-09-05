package com.example.oneshotai.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.ScreenTab

data class NavItem(
    val tab: ScreenTab,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
)

val navItems = listOf(
    NavItem(ScreenTab.CHAT, Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    NavItem(ScreenTab.PROJECTS, Icons.Filled.Folder, Icons.Outlined.Folder),
    NavItem(ScreenTab.AGENTS, Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    NavItem(ScreenTab.DEEPFIND, Icons.Filled.Search, Icons.Outlined.Search),
    NavItem(ScreenTab.TASKS, Icons.Filled.CheckCircle, Icons.Outlined.CheckCircleOutline),
    NavItem(ScreenTab.KNOWLEDGE, Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    NavItem(ScreenTab.TOOLS, Icons.Filled.Extension, Icons.Outlined.Extension)
)

@Composable
fun OneShotBottomBar(
    selectedTab: ScreenTab,
    isArabic: Boolean,
    onTabSelected: (ScreenTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        navItems.forEach { item ->
            val isSelected = selectedTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                        contentDescription = if (isArabic) item.tab.titleAr else item.tab.titleEn,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = if (isArabic) item.tab.titleAr else item.tab.titleEn,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = OneShotOrange,
                    indicatorColor = OneShotOrange,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
