package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object NavRoutes {
    const val HOME = "home"
    const val REPORTS = "reports"
    const val WORKERS = "workers"
    const val SETTINGS = "settings"
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = NavRoutes.HOME,
        title = "الرئيسية",
        icon = Icons.Default.Home
    )

    object Reports : BottomNavItem(
        route = NavRoutes.REPORTS,
        title = "التقارير",
        icon = Icons.Default.Assessment
    )

    object Workers : BottomNavItem(
        route = NavRoutes.WORKERS,
        title = "بيانات الخياطين والقصاصين",
        icon = Icons.Default.People
    )

    object Settings : BottomNavItem(
        route = NavRoutes.SETTINGS,
        title = "الإعدادات",
        icon = Icons.Default.Settings
    )

    companion object {
        val items = listOf(Home, Reports, Workers, Settings)
    }
}
