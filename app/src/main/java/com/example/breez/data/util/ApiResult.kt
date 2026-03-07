package com.example.breez.data.util

sealed class ApiResult<out T> {
    data object Loading : ApiResult<Nothing>()

    data class Success<T>(
        val data: T
    ) : ApiResult<T>()

    data class Error(
        val message: String,
        val code: Int? = null
    ) : ApiResult<Nothing>()
}