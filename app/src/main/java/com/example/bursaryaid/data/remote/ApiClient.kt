
package com.example.bursaryaid.data.remote

import com.example.bursaryaid.BuildConfig

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

import java.util.concurrent.TimeUnit

object ApiClient {

    private val client: OkHttpClient by lazy {

        val logging = HttpLoggingInterceptor().apply {

            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val service: BursaryApiService by lazy {

        val baseUrl = BuildConfig.BURSARY_API_BASE_URL

        require(
            baseUrl.startsWith("https://") ||
                    baseUrl.startsWith("http://")
        ) {
            "A valid API base URL must be configured."
        }

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(
                MoshiConverterFactory.create()
            )
            .build()
            .create(BursaryApiService::class.java)
    }
}
