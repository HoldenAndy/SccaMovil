package com.proyecto.scca.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageResponseDto<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean,
)
