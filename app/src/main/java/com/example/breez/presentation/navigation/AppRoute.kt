package com.example.breez.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute {

    @Serializable
    object SplashRoute : AppRoute()

    @Serializable
    object HomeRoute : AppRoute()

    @Serializable
    object SettingsRoute : AppRoute()

    @Serializable
    object FavoritesRoute : AppRoute()

    @Serializable
    object AlertsRoute : AppRoute()

    @Serializable
    object AddFavoriteRoute : AppRoute()

    @Serializable
    data class FavoriteDetailsRoute(
        val lat: Double,
        val lon: Double,
        val cityName: String = ""
    ) : AppRoute()

    @Serializable
    data class AddEditAlertRoute(
        val alertId: Long? = null
    ) : AppRoute()
}