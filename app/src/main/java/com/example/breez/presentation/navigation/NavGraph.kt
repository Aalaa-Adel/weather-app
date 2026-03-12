package com.example.breez.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.breez.presentation.SplashScreen
import com.example.breez.presentation.alerts.AddEditAlertScreen
import com.example.breez.presentation.alerts.AlertsScreen
import com.example.breez.presentation.favorites.AddFavoriteScreen
import com.example.breez.presentation.favorites.FavoriteDetailsScreen
import com.example.breez.presentation.favorites.FavoritesScreen
import com.example.breez.presentation.home.HomeScreen
import com.example.breez.presentation.settings.SettingsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry.value?.destination

    val showBottomBar = currentDestination?.let { destination ->
        destination.hasRoute<AppRoute.HomeRoute>() ||
                destination.hasRoute<AppRoute.SettingsRoute>() ||
                destination.hasRoute<AppRoute.FavoritesRoute>() ||
                destination.hasRoute<AppRoute.AlertsRoute>()
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BreezCurvedBottomBar(
                    modifier = Modifier.navigationBarsPadding(),
                    onHomeClick = {
                        navController.navigate(AppRoute.HomeRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCenterClick = {
                        navController.navigate(AppRoute.AlertsRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onFavoriteClick = {
                        navController.navigate(AppRoute.FavoritesRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onMenuClick = {
                        navController.navigate(AppRoute.SettingsRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAlarmClick = {
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHostContainer(
            navController = navController,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun NavHostContainer(
    navController: androidx.navigation.NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.SplashRoute
    ) {
        composable<AppRoute.SplashRoute> {
            SplashScreen(
                onFinish = {
                    navController.navigate(AppRoute.HomeRoute) {
                        popUpTo(AppRoute.SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.HomeRoute> {
            HomeScreen()
        }

        composable<AppRoute.SettingsRoute> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                bottomPadding = innerPadding
            )
        }

        composable<AppRoute.FavoritesRoute> {
            FavoritesScreen()
        }

        composable<AppRoute.AddFavoriteRoute> {
            AddFavoriteScreen()
        }

        composable<AppRoute.FavoriteDetailsRoute> {
            FavoriteDetailsScreen()
        }

        composable<AppRoute.AlertsRoute> {
            AlertsScreen()
        }

        composable<AppRoute.AddEditAlertRoute> {
            AddEditAlertScreen()
        }
    }
}