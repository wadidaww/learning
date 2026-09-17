package com.kresna.aitopicapp.di

import com.kresna.aitopicapp.data.local.SettingsDataStore
import com.kresna.aitopicapp.data.remote.BytePlusApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        settingsDataStore: SettingsDataStore
    ): Retrofit {
        val settings = runBlocking { settingsDataStore.getSettings() }
        return Retrofit.Builder()
            .baseUrl(settings.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBytePlusApi(
        okHttpClient: OkHttpClient,
        settingsDataStore: SettingsDataStore
    ): BytePlusApi {
        val settings = runBlocking { settingsDataStore.getSettings() }
        val authClient = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer ${settings.apiKey}")
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(settings.baseUrl)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BytePlusApi::class.java)
    }
}
