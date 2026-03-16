package com.example.breez.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.example.breez.data.datasource.preferences.LocationSource
import com.example.breez.presentation.SplashScreen
import com.example.breez.presentation.alerts.AddEditAlertScreen
import com.example.breez.presentation.alerts.AlertsScreen
import com.example.breez.presentation.favorites.AddFavoriteScreen
import com.example.breez.presentation.favorites.FavoriteDetailsScreen
import com.example.breez.presentation.favorites.FavoritesScreen
import com.example.breez.presentation.home.HomeScreen
import com.example.breez.presentation.location.MapboxPickLocationScreen
import com.example.breez.presentation.settings.SettingsScreen
import com.example.breez.presentation.settings.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry.value?.destination

    val homeViewModel: com.example.breez.presentation.home.HomeViewModel = hiltViewModel()

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
                        navController.navigate(AppRoute.PickLocationRoute()) {
                            launchSingleTop = true
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
                        navController.navigate(AppRoute.AlertsRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHostContainer(
            navController = navController,
            innerPadding = innerPadding,
            homeViewModel = homeViewModel
        )
    }
}

@Composable
private fun NavHostContainer(
    navController: androidx.navigation.NavHostController,
    innerPadding: PaddingValues,
    homeViewModel: com.example.breez.presentation.home.HomeViewModel
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
            HomeScreenWithPermission(viewModel = homeViewModel)
        }

        composable<AppRoute.SettingsRoute> { entry ->
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val savedStateHandle = entry.savedStateHandle
            val pickedLat = savedStateHandle.get<Double>("lat")
            val pickedLon = savedStateHandle.get<Double>("lon")

            LaunchedEffect(pickedLat, pickedLon) {
                if (pickedLat != null && pickedLon != null) {
                    settingsViewModel.saveHomeLocation(pickedLat.toFloat(), pickedLon.toFloat())
                    savedStateHandle.remove<Double>("lat")
                    savedStateHandle.remove<Double>("lon")
                }
            }

            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                bottomPadding = innerPadding,
                onNavigateToPickLocation = {
                    val (homeLat, homeLon) = settingsViewModel.getCurrentHomeLocation()
                    navController.navigate(AppRoute.PickLocationRoute(initialLat = homeLat, initialLon = homeLon))
                },
                viewModel = settingsViewModel
            )
        }


        composable<AppRoute.FavoritesRoute> {
            FavoritesScreen(
                onNavigateToAddFavorite = {
                    navController.navigate(AppRoute.AddFavoriteRoute)
                },
                onNavigateToFavoriteDetails = { lat, lon, cityName, favoriteId ->
                    navController.navigate(AppRoute.FavoriteDetailsRoute(lat, lon, cityName, favoriteId))
                }
            )
        }

        composable<AppRoute.AddFavoriteRoute> {
            AddFavoriteScreen(
                onBackClick = { navController.popBackStack() },
                onFavoriteSaved = { navController.popBackStack() }
            )
        }

        composable<AppRoute.FavoriteDetailsRoute> { entry ->
            val args = entry.toRoute<AppRoute.FavoriteDetailsRoute>()
            FavoriteDetailsScreen(
                lat = args.lat,
                lon = args.lon,
                cityName = args.cityName,
                favoriteId = args.favoriteId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<AppRoute.AlertsRoute> {
            AlertsScreen(
//                onNavigateToAddAlert = {
//                    navController.navigate(AppRoute.AddEditAlertRoute())
//                },
//                onNavigateToEditAlert = { alertId ->
//                    navController.navigate(AppRoute.AddEditAlertRoute(alertId))
//                }
            )
        }

        composable<AppRoute.AddEditAlertRoute> { entry ->
            val args = entry.toRoute<AppRoute.AddEditAlertRoute>()
            AddEditAlertScreen(
//                alertId = args.alertId,
//                onBackClick = { navController.popBackStack() },
//                onAlertSaved = { navController.popBackStack() }
            )
        }

        composable<AppRoute.PickLocationRoute> { entry ->
            val args = entry.toRoute<AppRoute.PickLocationRoute>()

            MapboxPickLocationScreen(
                initialLat = args.initialLat,
                initialLon = args.initialLon,
                onConfirm = { lat: Double, lon: Double ->
                    navController.previousBackStackEntry?.savedStateHandle?.set<Double>("lat", lat)
                    navController.previousBackStackEntry?.savedStateHandle?.set<Double>("lon", lon)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun HomeScreenWithPermission(
    viewModel: com.example.breez.presentation.home.HomeViewModel
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val uiState = settingsViewModel.uiState.collectAsStateWithLifecycle().value

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (uiState.locationSource == LocationSource.GPS) {
            val hasFineLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasFineLocation && !hasCoarseLocation) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    HomeScreen(viewModel = viewModel)
}