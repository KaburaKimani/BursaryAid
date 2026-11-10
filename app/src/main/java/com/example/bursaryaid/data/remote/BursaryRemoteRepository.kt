package com.example.bursaryaid.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : ApiResult<Nothing>()
}

class BursaryRemoteRepository(
    private val api: BursaryApiService = ApiClient.service
) {
    suspend fun fetchOpenBursaries(): ApiResult<List<BursaryDto>> = withContext(Dispatchers.IO) {
        try {
            val list = api.getBursaries()
            // Filter to only show bursaries that are currently open
            ApiResult.Success(list.filter { it.isBursaryOpen })
        } catch (e: IOException) {
            ApiResult.Error("Network unavailable. Check your connection.", e)
        } catch (e: Exception) {
            ApiResult.Error("Unexpected error: ${e.message}", e)
        }
    }
}