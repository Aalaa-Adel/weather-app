package com.example.breez.di


import android.content.Context
import androidx.room.Room
import com.example.breez.data.datasource.local.LocalDataSource
import com.example.breez.data.datasource.local.LocalDataSourceImpl
import com.example.breez.data.datasource.remote.WeatherApiService
import com.example.breez.data.datasource.remote.RemoteDataSource
import com.example.breez.data.datasource.remote.WeatherRemoteDataSource
import com.example.breez.data.datasource.preferences.SettingsPreferencesManager
import com.example.breez.data.location.LocationProvider
import com.example.breez.data.network.NetworkMonitor
import com.example.breez.data.db.BreezDatabase
import com.example.breez.data.db.dao.FavoriteDao
import com.example.breez.data.db.dao.AlertDao
import com.example.breez.data.db.dao.WeatherCacheDao
import com.example.breez.data.notification.AlertScheduler
import com.example.breez.data.notification.NotificationHelper
import com.example.breez.data.notification.WeatherAlarmManager
import com.example.breez.data.repository.BreezRepository
import com.example.breez.data.repository.BreezRepositoryImpl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.openweathermap.org/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBreezDatabase(@ApplicationContext context: Context): BreezDatabase {
        return Room.databaseBuilder(
            context,
            BreezDatabase::class.java,
            BreezDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherRemoteDataSource(
        apiService: WeatherApiService
    ): RemoteDataSource {
        return WeatherRemoteDataSource(apiService)
    }

    @Provides
    fun provideFavoriteDao(database: BreezDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun provideAlertDao(database: BreezDatabase): AlertDao {
        return database.alertDao()
    }

    @Provides
    fun provideWeatherCacheDao(database: BreezDatabase): WeatherCacheDao {
        return database.weatherCacheDao()
    }

    @Provides
    @Singleton
    fun provideLocalDataSource(
        favoriteDao: FavoriteDao,
        alertDao: AlertDao,
        weatherCacheDao: WeatherCacheDao
    ): LocalDataSource {
        return LocalDataSourceImpl(favoriteDao, alertDao, weatherCacheDao)
    }

    @Provides
    @Singleton
    fun provideBreezRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        gson: Gson
    ): BreezRepository {
        return BreezRepositoryImpl(remoteDataSource, localDataSource, gson)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideLocationProvider(@ApplicationContext context: Context): LocationProvider {
        return LocationProvider(context)
    }

    @Provides
    @Singleton
    fun provideSettingsPreferencesManager(@ApplicationContext context: Context): SettingsPreferencesManager {
        return SettingsPreferencesManager(context)
    }

    @Provides @Singleton
    fun provideWeatherAlarmManager(@ApplicationContext context: Context): WeatherAlarmManager =
        WeatherAlarmManager(context)

    @Provides @Singleton
    fun provideAlertScheduler(
        @ApplicationContext context: Context,
        weatherAlarmManager: WeatherAlarmManager
    ): AlertScheduler = AlertScheduler(context, weatherAlarmManager)


    @Provides @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper =
        NotificationHelper(context)

}