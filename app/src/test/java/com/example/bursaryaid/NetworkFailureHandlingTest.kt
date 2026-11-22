package com.example.bursaryaid

import com.example.bursaryaid.data.remote.ApiResult
import com.example.bursaryaid.data.remote.BursaryApiService
import com.example.bursaryaid.data.remote.BursaryDto
import com.example.bursaryaid.data.remote.BursaryRemoteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkFailureHandlingTest {

    private class FakeApiService(
        private val shouldFail: Boolean = false,
        private val bursaries: List<BursaryDto> = emptyList()
    ) : BursaryApiService {
        override suspend fun getBursaries(): List<BursaryDto> {
            if (shouldFail) {
                throw IOException("Simulated network loss")
            }
            return bursaries
        }
    }

    @Test
    fun testNetworkFailureHandling_ioException_returnsErrorResult() = runBlocking {
        val fakeApi = FakeApiService(shouldFail = true)
        val repo = BursaryRemoteRepository(fakeApi)

        val result = repo.fetchOpenBursaries()

        assertTrue(result is ApiResult.Error)
        val errorResult = result as ApiResult.Error
        assertEquals("Network unavailable. Check your connection.", errorResult.message)
    }

    @Test
    fun testNetworkFailureHandling_successfulResponse_returnsOpenOnly() = runBlocking {
        val mockList = listOf(
            BursaryDto(
                bursaryId = 1L,
                bursaryName = "Bursary A",
                bursaryDescription = "Desc A",
                fundingOrganization = "Org A",
                totalBudgetKes = 100000L,
                applicationDeadlineMillis = 2000000000L,
                isBursaryOpen = true
            ),
            BursaryDto(
                bursaryId = 2L,
                bursaryName = "Bursary B",
                bursaryDescription = "Desc B",
                fundingOrganization = "Org B",
                totalBudgetKes = 50000L,
                applicationDeadlineMillis = 2000000000L,
                isBursaryOpen = false
            )
        )
        val fakeApi = FakeApiService(shouldFail = false, bursaries = mockList)
        val repo = BursaryRemoteRepository(fakeApi)

        val result = repo.fetchOpenBursaries()

        assertTrue(result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertEquals(1, successResult.data.size)
        assertEquals("Bursary A", successResult.data[0].bursaryName)
    }
}
