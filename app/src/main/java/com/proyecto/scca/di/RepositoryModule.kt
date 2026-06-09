package com.proyecto.scca.di

import com.proyecto.scca.data.repository.AnalisisRepositoryImpl
import com.proyecto.scca.data.repository.AuthRepositoryImpl
import com.proyecto.scca.data.repository.ImagenRepositoryImpl
import com.proyecto.scca.data.repository.LecturaRepositoryImpl
import com.proyecto.scca.data.repository.LogRepositoryImpl
import com.proyecto.scca.data.repository.NodoRepositoryImpl
import com.proyecto.scca.data.repository.RealtimeRepositoryImpl
import com.proyecto.scca.data.repository.UsuarioRepositoryImpl
import com.proyecto.scca.domain.repository.AnalisisRepository
import com.proyecto.scca.domain.repository.AuthRepository
import com.proyecto.scca.domain.repository.ImagenRepository
import com.proyecto.scca.domain.repository.LecturaRepository
import com.proyecto.scca.domain.repository.LogRepository
import com.proyecto.scca.domain.repository.NodoRepository
import com.proyecto.scca.domain.repository.RealtimeRepository
import com.proyecto.scca.domain.repository.UsuarioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNodoRepository(impl: NodoRepositoryImpl): NodoRepository

    @Binds
    @Singleton
    abstract fun bindLecturaRepository(impl: LecturaRepositoryImpl): LecturaRepository

    @Binds
    @Singleton
    abstract fun bindAnalisisRepository(impl: AnalisisRepositoryImpl): AnalisisRepository

    @Binds
    @Singleton
    abstract fun bindImagenRepository(impl: ImagenRepositoryImpl): ImagenRepository

    @Binds
    @Singleton
    abstract fun bindUsuarioRepository(impl: UsuarioRepositoryImpl): UsuarioRepository

    @Binds
    @Singleton
    abstract fun bindLogRepository(impl: LogRepositoryImpl): LogRepository

    @Binds
    @Singleton
    abstract fun bindRealtimeRepository(impl: RealtimeRepositoryImpl): RealtimeRepository
}
