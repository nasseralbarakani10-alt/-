package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.BottomNavItem
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkersScreen
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.CuttersViewModel
import com.example.ui.viewmodel.OrdersViewModel
import com.example.ui.viewmodel.TailorsViewModel

@Composable
fun MainAppContainer(
    authViewModel: AuthViewModel,
    ordersViewModel: OrdersViewModel,
    categoriesViewModel: CategoriesViewModel,
    cuttersViewModel: CuttersViewModel,
    tailorsViewModel: TailorsViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isSessionLoading by authViewModel.isSessionLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentPermissions by authViewModel.currentPermissions.collectAsState()

    if (isSessionLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = BluePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        return
    }

    if (!isLoggedIn) {
        LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = { }
        )
        return
    }

    // Filter bottom navigation items based on user permissions
    val isAdmin = currentUser?.isAdmin == true
    val canAccessReports = isAdmin || (currentPermissions?.canAccessReports == true)
    val canAccessSettings = isAdmin || (currentPermissions?.canAccessSettings == true)

    val visibleNavItems = remember(isAdmin, canAccessReports, canAccessSettings) {
        listOfNotNull(
            BottomNavItem.Home,
            if (canAccessReports) BottomNavItem.Reports else null,
            BottomNavItem.Workers,
            if (canAccessSettings) BottomNavItem.Settings else null
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    visibleNavItems.forEach { item ->
                        val selected = currentDestination?.route == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentDestination?.route != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BluePrimary,
                                selectedTextColor = BluePrimary,
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B),
                                indicatorColor = Color(0xFFE3F2FD)
                            ),
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.HOME,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(NavRoutes.HOME) {
                    MainScreen(
                        ordersViewModel = ordersViewModel,
                        categoriesViewModel = categoriesViewModel,
                        cuttersViewModel = cuttersViewModel,
                        tailorsViewModel = tailorsViewModel
                    )
                }
                composable(NavRoutes.REPORTS) {
                    ReportsScreen(
                        authViewModel = authViewModel,
                        ordersViewModel = ordersViewModel,
                        categoriesViewModel = categoriesViewModel,
                        cuttersViewModel = cuttersViewModel,
                        tailorsViewModel = tailorsViewModel
                    )
                }
                composable(NavRoutes.WORKERS) {
                    WorkersScreen(
                        cuttersViewModel = cuttersViewModel,
                        tailorsViewModel = tailorsViewModel,
                        categoriesViewModel = categoriesViewModel,
                        ordersViewModel = ordersViewModel
                    )
                }
                composable(NavRoutes.SETTINGS) {
                    SettingsScreen(
                        categoriesViewModel = categoriesViewModel,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
