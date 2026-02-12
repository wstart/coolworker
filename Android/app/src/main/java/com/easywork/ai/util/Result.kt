package com.easywork.ai.util

/**
 * 通用结果封装
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Exception? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

/**
 * 将Result转换为Resource
 */
fun <T> Result<T>.toResource(): Resource<T> {
    return if (isSuccess) {
        Resource.Success(getOrNull()!!)
    } else {
        Resource.Error(
            exceptionOrNull()?.message ?: "Unknown error",
            exceptionOrNull() as? Exception
        )
    }
}
