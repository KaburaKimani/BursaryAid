
package com.example.bursaryaid.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import retrofit2.HttpException

import java.io.IOException

sealed class ApiResult<out T> {

    data class Success<T>(
        val data: T
    ) : ApiResult<T>()

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ApiResult<Nothing>()
}

class BursaryRemoteRepository(
    private val api: BursaryApiService = ApiClient.service
) {

    suspend fun fetchOpenBursaries(): ApiResult<List<BursaryDto>> =
        withContext(Dispatchers.IO) {

            try {

                val bursaries = api.getBursaries()

                ApiResult.Success(
                    bursaries.filter { it.isBursaryOpen }
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: IOException) {

                ApiResult.Error(
                    "Network unavailable. Check your connection.",
                    e
                )

            } catch (e: HttpException) {

                ApiResult.Error(
                    "Server error (${e.code()}). Please try again.",
                    e
                )

            } catch (e: Exception) {

                ApiResult.Error(
                    "Unable to retrieve bursaries. Please try again.",
                    e
                )
            }
        }
}
