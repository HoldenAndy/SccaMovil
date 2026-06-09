package com.proyecto.scca.data.mapper

import com.proyecto.scca.data.remote.dto.LecturaDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class LecturaMapperTest {
    private val fechaHora = LocalDateTime(2026, 5, 20, 21, 58, 0)

    private fun dto() =
        LecturaDto(
            idLectura = 12,
            idNodo = 3,
            ph = 7.2,
            temperatura = 24.5,
            turbidez = 1.8,
            tds = 320.0,
            fechaHora = fechaHora,
        )

    @Test
    fun `LecturaDto toDomain mapea campos correctamente`() {
        val dominio = dto().toDomain()
        assertEquals(12, dominio.idLectura)
        assertEquals(3, dominio.idNodo)
        assertEquals(7.2, dominio.ph, 0.001)
        assertEquals(24.5, dominio.temperatura, 0.001)
        assertEquals(1.8, dominio.turbidez, 0.001)
        assertEquals(320.0, dominio.tds, 0.001)
        assertEquals(fechaHora, dominio.fechaHora)
    }

    @Test
    fun `PageResponseDto LecturaDto toDomain mapea paginacion`() {
        val page =
            PageResponseDto(
                content = listOf(dto()),
                pageNumber = 0,
                pageSize = 8,
                totalElements = 42L,
                totalPages = 6,
                isLast = false,
            )
        val dominio = page.toDomain()
        assertEquals(1, dominio.contenido.size)
        assertEquals(0, dominio.numeroPagina)
        assertEquals(8, dominio.tamanioPagina)
        assertEquals(42L, dominio.totalElementos)
        assertEquals(6, dominio.totalPaginas)
        assertFalse(dominio.esUltima)
    }
}
