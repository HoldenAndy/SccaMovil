package com.proyecto.scca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.proyecto.scca.core.session.PreferenciasStore
import com.proyecto.scca.presentation.navigation.SccaNavHost
import com.proyecto.scca.presentation.theme.SccaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferenciasStore: PreferenciasStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePref by preferenciasStore.themeFlow.collectAsState(initial = "system")
            val systemDark = isSystemInDarkTheme()
            val darkTheme =
                when (themePref) {
                    "light" -> false
                    "dark" -> true
                    else -> systemDark
                }

            SccaTheme(darkTheme = darkTheme) {
                SccaNavHost()
            }
        }
    }
}
