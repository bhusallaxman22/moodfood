package com.example.moodfood.data.ai

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response as OkHttpResponse
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response as RetrofitResponse
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import android.util.Log

interface OpenRouterService {
    @Headers("Content-Type: application/json")
    @POST("/api/v1/chat/completions")
    suspend fun chat(@Body body: String): RetrofitResponse<String>
}

class AuthInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): OkHttpResponse {
        Log.d("OpenRouter", "API key length: ${apiKey.length}, starts with: ${apiKey.take(15)}")
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        Log.d("OpenRouter", "Authorization header: ${req.header("Authorization")}")
        return chain.proceed(req)
    }
}

object OpenRouterClientFactory {
    fun create(apiKey: String): OpenRouterService {
        Log.d("OpenRouterFactory", "Creating client with API key length: ${apiKey.length}")
        Log.d("OpenRouterFactory", "API key starts with: ${apiKey.take(20)}")
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Changed to BODY to see full request/response
            redactHeader("Authorization")
        }
        val http = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(apiKey))
            .addInterceptor(logging)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .client(http)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit.create(OpenRouterService::class.java)
    }
}
