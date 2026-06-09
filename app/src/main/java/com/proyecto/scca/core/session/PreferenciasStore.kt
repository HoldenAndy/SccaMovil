package com.proyecto.scca.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scca_preferencias")

@Singleton
class PreferenciasStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            val THEME_KEY = stringPreferencesKey("theme")
            val DENSITY_KEY = stringPreferencesKey("density")
            val LIVE_TAIL_KEY = booleanPreferencesKey("live_tail")
            val ULTIMO_NODO_KEY = stringPreferencesKey("ultimo_nodo")
        }

        val themeFlow: Flow<String> =
            context.dataStore.data.map { prefs ->
                prefs[THEME_KEY] ?: "system"
            }

        val densityFlow: Flow<String> =
            context.dataStore.data.map { prefs ->
                prefs[DENSITY_KEY] ?: "comfortable"
            }

        val liveTailFlow: Flow<Boolean> =
            context.dataStore.data.map { prefs ->
                prefs[LIVE_TAIL_KEY] ?: false
            }

        val ultimoNodoFlow: Flow<String?> =
            context.dataStore.data.map { prefs ->
                prefs[ULTIMO_NODO_KEY]
            }

        suspend fun setTheme(theme: String) {
            context.dataStore.edit { prefs ->
                prefs[THEME_KEY] = theme
            }
        }

        suspend fun setDensity(density: String) {
            context.dataStore.edit { prefs ->
                prefs[DENSITY_KEY] = density
            }
        }

        suspend fun setLiveTail(enabled: Boolean) {
            context.dataStore.edit { prefs ->
                prefs[LIVE_TAIL_KEY] = enabled
            }
        }

        suspend fun setUltimoNodo(idNodo: String) {
            context.dataStore.edit { prefs ->
                prefs[ULTIMO_NODO_KEY] = idNodo
            }
        }
    }
