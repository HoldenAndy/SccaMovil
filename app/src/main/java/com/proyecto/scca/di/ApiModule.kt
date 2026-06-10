package com.proyecto.scca.di

import com.proyecto.scca.core.network.AnalisisClientQualifier
import com.proyecto.scca.data.remote.api.AnalisisApi
import com.proyecto.scca.data.remote.api.AuthApi
import com.proyecto.scca.data.remote.api.ImagenApi
import com.proyecto.scca.data.remote.api.LecturaApi
import com.proyecto.scca.data.remote.api.LogApi
import com.proyecto.scca.data.remote.api.NodoApi
import com.proyecto.scca.data.remote.api.UsuarioApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideNodoApi(retrofit: Retrofit): NodoApi = retrofit.create(NodoApi::class.java)

    @Provides
    @Singleton
    fun provideLecturaApi(retrofit: Retrofit): LecturaApi = retrofit.create(LecturaApi::class.java)

    @Provides
    @Singleton
    fun provideAnalisisApi(
        @AnalisisClientQualifier retrofit: Retrofit,
    ): AnalisisApi = retrofit.create(AnalisisApi::class.java)

    @Provides
    @Singleton
    fun provideImagenApi(retrofit: Retrofit): ImagenApi = retrofit.create(ImagenApi::class.java)

    @Provides
    @Singleton
    fun provideUsuarioApi(retrofit: Retrofit): UsuarioApi = retrofit.create(UsuarioApi::class.java)

    @Provides
    @Singleton
    fun provideLogApi(retrofit: Retrofit): LogApi = retrofit.create(LogApi::class.java)
}
