package com.example.breez.di

import android.content.Context
import com.example.breez.data.datasource.remote.WeatherApiService
import com.example.breez.data.datasource.remote.WeatherRemoteDataSource
import com.example.breez.data.repository.WeatherRepository
import com.example.breez.data.repository.WeatherRepositoryImpl
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
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherRemoteDataSource(
        @ApplicationContext context: Context,
        apiService: WeatherApiService
    ): WeatherRemoteDataSource {
        return WeatherRemoteDataSource(context, apiService)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(remoteDataSource: WeatherRemoteDataSource): WeatherRepository {
        return WeatherRepositoryImpl(remoteDataSource)
    }


}