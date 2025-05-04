package com.project.postalapp.chatbot

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface PincodeApiService {
    @GET("postoffice/{post_office_name}")
    suspend fun getPostOfficesByName(@Path("post_office_name") postOfficeName: String): List<PincodeResponse>

    companion object {
        private const val BASE_URL = "https://api.postalpincode.in/"

        fun create(): PincodeApiService {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PincodeApiService::class.java)
        }
    }
}