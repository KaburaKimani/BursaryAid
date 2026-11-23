package com.example.bursaryaid.data.remote

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : ApiResult<Nothing>()
}

data class BursaryDto(
    val bursaryId: Long,
    val bursaryName: String,
    val bursaryDescription: String,
    val fundingOrganization: String,
    val totalBudgetKes: Long,
    val applicationDeadlineMillis: Long,
    val isBursaryOpen: Boolean
)

interface BursaryApiService {
    suspend fun getBursaries(): List<BursaryDto>
}

class BursaryRemoteRepository(
    private val apiService: BursaryApiService
) {
    suspend fun fetchOpenBursaries(): ApiResult<List<BursaryDto>> {
        return try {
            val bursaries = apiService.getBursaries()
            val openBursaries = bursaries.filter { it.isBursaryOpen }
            ApiResult.Success(openBursaries)
        } catch (e: Exception) {
            ApiResult.Error("Network unavailable. Check your connection.", e)
        }
    }
}
