package com.proyecto.scca.core.util

object Constantes {
    object Api {
        const val TIMEOUT_MS = 15_000L
        const val TIMEOUT_ANALISIS_MS = 60_000L
    }

    object Dashboard {
        const val CHART_WINDOW_MS = 7_200_000L // 2 horas
        const val POLLING_MS = 30_000L
    }

    object Paginacion {
        const val TABLE_PAGE_SIZE = 8
        const val ANALYSIS_PAGE_SIZE = 50
    }

    object DateRange {
        const val HISTORY_DAYS = 7
    }
}
