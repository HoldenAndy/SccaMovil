package com.proyecto.scca.core.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.SesionUsuario
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private const val PREF_FILE = "scca_session_prefs"
            private const val KEY_TOKEN = "token"
            private const val KEY_NOMBRE = "nombre"
            private const val KEY_ROL = "rol"
            private const val KEY_DEBE_CAMBIAR = "debe_cambiar_password"
        }

        private val masterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        private val prefs =
            try {
                EncryptedSharedPreferences.create(
                    context,
                    PREF_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            } catch (e: java.security.GeneralSecurityException) {
                android.util.Log.e("SessionManager", "Security error reading encrypted prefs", e)
                context.deleteSharedPreferences(PREF_FILE)
                try {
                    EncryptedSharedPreferences.create(
                        context,
                        PREF_FILE,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                    )
                } catch (e2: java.security.GeneralSecurityException) {
                    android.util.Log.e("SessionManager", "Irrecoverable Keystore error", e2)
                    // KeyStore irrecuperable: fallback a prefs sin cifrado para no crashear la app
                    context.getSharedPreferences(PREF_FILE + "_plain", android.content.Context.MODE_PRIVATE)
                } catch (e2: java.io.IOException) {
                    android.util.Log.e("SessionManager", "IO error reading encrypted prefs retry", e2)
                    context.getSharedPreferences(PREF_FILE + "_plain", android.content.Context.MODE_PRIVATE)
                }
            } catch (e: java.io.IOException) {
                android.util.Log.e("SessionManager", "IO error reading encrypted prefs", e)
                context.deleteSharedPreferences(PREF_FILE)
                try {
                    EncryptedSharedPreferences.create(
                        context,
                        PREF_FILE,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                    )
                } catch (e2: java.security.GeneralSecurityException) {
                    android.util.Log.e("SessionManager", "Irrecoverable Keystore error", e2)
                    context.getSharedPreferences(PREF_FILE + "_plain", android.content.Context.MODE_PRIVATE)
                } catch (e2: java.io.IOException) {
                    android.util.Log.e("SessionManager", "IO error reading encrypted prefs retry", e2)
                    context.getSharedPreferences(PREF_FILE + "_plain", android.content.Context.MODE_PRIVATE)
                }
            }

        private val _tokenFlow = MutableStateFlow<String?>(prefs.getString(KEY_TOKEN, null))
        val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

        private val _sessionFlow = MutableStateFlow<SesionUsuario?>(cargarSesionDesdePrefs())
        val sessionFlow: StateFlow<SesionUsuario?> = _sessionFlow.asStateFlow()

        private val _eventoLogout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val eventoLogout: SharedFlow<Unit> = _eventoLogout.asSharedFlow()

        val rolActual: Rol?
            get() = _sessionFlow.value?.rol

        val tokenActual: String?
            get() = _tokenFlow.value

        fun guardarSesion(
            token: String,
            nombre: String,
            rol: Rol,
            debeCambiarPassword: Boolean,
        ) {
            prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_NOMBRE, nombre)
                .putString(KEY_ROL, rol.name)
                .putBoolean(KEY_DEBE_CAMBIAR, debeCambiarPassword)
                .apply()
            _tokenFlow.value = token
            _sessionFlow.value = SesionUsuario(nombre, rol, debeCambiarPassword)
        }

        fun marcarPasswordCambiado() {
            prefs.edit().putBoolean(KEY_DEBE_CAMBIAR, false).apply()
            _sessionFlow.value = _sessionFlow.value?.copy(debeCambiarPassword = false)
        }

        fun logout() {
            prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_NOMBRE)
                .remove(KEY_ROL)
                .remove(KEY_DEBE_CAMBIAR)
                .apply()
            _tokenFlow.value = null
            _sessionFlow.value = null
            _eventoLogout.tryEmit(Unit)
        }

        fun isAuthenticated(): Boolean = _tokenFlow.value != null

        private fun cargarSesionDesdePrefs(): SesionUsuario? {
            val nombre = prefs.getString(KEY_NOMBRE, null)
            val rolStr = prefs.getString(KEY_ROL, null)
            val debeCambiar = prefs.getBoolean(KEY_DEBE_CAMBIAR, false)
            val rol = rolStr?.let { runCatching { Rol.valueOf(it) }.getOrNull() }
            return if (nombre != null && rol != null) {
                SesionUsuario(nombre, rol, debeCambiar)
            } else {
                null
            }
        }
    }
