package com.example.breez.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
            SettingsScreen()
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