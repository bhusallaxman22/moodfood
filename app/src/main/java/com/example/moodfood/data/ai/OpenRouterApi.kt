package com.example.moodfood.data.ai

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenRouterService {
    @Headers("Content-Type: application/json")
    @POST("/api/v1/chat/completions")
    suspend fun chat(@Body body: String): String
}

class AuthInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${'$'}apiKey")
            .addHeader("HTTP-Referer", "https://example.com")
            .addHeader("X-Title", "MoodFood")
            .build()
        return chain.proceed(req)
    }
}

object OpenRouterClientFactory {
    fun create(apiKey: String): OpenRouterService {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val http = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(apiKey))
            .addInterceptor(logging)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai")
            .client(http)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit.create(OpenRouterService::class.java)
    }
}
