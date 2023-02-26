package com.ionnier.pdma.di

import android.content.Context
import com.ionnier.pdma.MainApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    @Singleton
    fun provideContext() = MainApplication.appContext

    @Provides
    @Singleton
    fun provideHttpClient() = HttpClient(CIO) {
        install(Logging) {
            logger = object: Logger {
                override fun log(message: String) {
                    Timber.tag("HTTP")
                    Timber.w(message)
                }
            }
            level = LogLevel.INFO
        }
        install(ContentNegotiation) {
            json()
        }
    }
}
