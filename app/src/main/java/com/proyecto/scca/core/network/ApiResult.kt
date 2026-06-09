package com.proyecto.scca.core.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()

    data class Error(val code: Int? = null, val message: String) : ApiResult<Nothing>()

    data object Loading : ApiResult<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun toResult(): Result<T> =
        when (this) {
            is Success -> Result.success(data)
            is Error -> Result.failure(Exception(message))
            Loading -> Result.failure(Exception("Loading"))
        }
}

fun <T> ApiResult<T>.map(transform: (T) -> T): ApiResult<T> =
    when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(data))
        is ApiResult.Error -> this
        ApiResult.Loading -> this
    }
