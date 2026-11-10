package com.example.bursaryaid.data.remote

import retrofit2.http.GET

interface BursaryApiService {
    @GET("bursaries")
    suspend fun getBursaries(): List<BursaryDto>
}{
}