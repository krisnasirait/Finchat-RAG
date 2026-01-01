package com.krisna.finchat

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class ChatRequest(val question: String)

data class ChatResponse(
    val answer: String,
    val sources: List<String>? = null
)

interface FinChatApi {
    @Multipart
    @POST("upload")
    suspend fun uploadPdf(
        @Part file: MultipartBody.Part
    ): Map<String, Any>

    @POST("chat")
    suspend fun chat(
        @Body request: ChatRequest
    ): ChatResponse
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: FinChatApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FinChatApi::class.java)
}