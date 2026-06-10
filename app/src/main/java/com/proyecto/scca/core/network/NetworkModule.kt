package com.proyecto.scca.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.proyecto.scca.BuildConfig
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.core.util.Constantes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Cliente OkHttp dedicado a las conexiones SSE.
 *
 * Un stream SSE es una respuesta HTTP "infinita": no se debe usar el cliente REST,
 * porque:
 *  - HttpLoggingInterceptor.Level.BODY bufferiza el cuerpo completo antes de
 *    entregarlo, y al no terminar nunca, no entrega ningun evento.
 *  - readTimeout(15s) corta la conexion antes del ping del backend (cada 25s),
 *    provocando un bucle de reconexion.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SseClientQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AnalisisClientQualifier

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.HEADERS
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    // --- Cliente REST (sin cambios) ---
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // --- Cliente dedicado a SSE: sin logging de BODY y sin readTimeout ---
    @Provides
    @Singleton
    @SseClientQualifier
    fun provideSseOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // NO se anade el loggingInterceptor de BODY
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // stream infinito: sin timeout de lectura
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .pingInterval(20, TimeUnit.SECONDS) // mantiene viva la conexion
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // --- Cliente dedicado a AnalisisApi: readTimeout extendido para llamadas a LLM ---
    @Provides
    @Singleton
    @AnalisisClientQualifier
    fun provideAnalisisOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(Constantes.Api.TIMEOUT_ANALISIS_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @AnalisisClientQualifier
    fun provideAnalisisRetrofit(
        @AnalisisClientQualifier okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
