package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(val contents: List<Content>)

@JsonClass(generateAdapter = true)
data class PartResponse(val text: String?)

@JsonClass(generateAdapter = true)
data class ContentResponse(val parts: List<PartResponse>?)

@JsonClass(generateAdapter = true)
data class Candidate(val content: ContentResponse?)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiService = retrofit.create(GeminiService::class.java)

    /**
     * Call the Gemini API to generate content. Fallback to offline puzzles if API key is invalid or fails.
     */
    suspend fun askOracle(prompt: String, fallbackRiddle: String): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return fallbackRiddle
        }

        return try {
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                )
            )
            val response = service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: fallbackRiddle
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackRiddle
        }
    }
}
