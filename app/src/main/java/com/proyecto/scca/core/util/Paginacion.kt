package com.proyecto.scca.core.util

/**
 * Port de buildPageNumbers de la web.
 * Devuelve lista de numeros de pagina a mostrar (1-indexed para UI).
 */
object Paginacion {
    const val TABLE_PAGE_SIZE = 8
    const val ANALYSIS_PAGE_SIZE = 50
    const val HISTORY_DAYS = 7

    fun buildPageNumbers(
        currentPage: Int,
        totalPages: Int,
        visibles: Int = 5,
    ): List<Int> {
        if (totalPages <= 0) return emptyList()
        if (totalPages <= visibles) return (1..totalPages).toList()

        val half = visibles / 2
        val start = (currentPage - half).coerceAtLeast(1)
        val end = (start + visibles - 1).coerceAtMost(totalPages)
        val adjustedStart = (end - visibles + 1).coerceAtLeast(1)
        return (adjustedStart..end).toList()
    }
}
