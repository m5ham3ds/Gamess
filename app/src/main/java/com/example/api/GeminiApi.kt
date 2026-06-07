package com.example.api

import com.example.game.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response Data Classes ---

data class Part(
    val text: String
)

data class Content(
    val parts: List<Part>
)

data class ResponseFormatText(
    val mimeType: String
)

data class ResponseFormat(
    val text: ResponseFormatText? = null
)

data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: String? = null,
    val temperature: Float? = 0.9f
)

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Candidate(
    val content: Content
)

data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

// --- Temple Riddle Domain Class ---
data class TempleRiddle(
    val riddle: String,
    val choices: List<String>,
    val correctIndex: Int
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Fetch a multiple choice riddle about shadows, souls, or forgetfulness.
     */
    suspend fun fetchOracleRiddle(level: Int): TempleRiddle = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            return@withContext getFallbackRiddle(level)
        }

        val prompt = """
            Generate an enigmatic riddle for the Eldritch Temple Oracle in a dark-theme hollow-knight inspired video game.
            The riddle should be about souls, shadows, memories, oblivion, or void.
            Generate it in Arabic. Provide exactly 4 potential multiple-choice answers in Arabic. One must be correct.
            Return ONLY a valid JSON object matching this exact schema:
            {
               "riddle": "the riddle text in Arabic",
               "choices": ["Option A text", "Option B text", "Option C text", "Option D text"],
               "correctIndex": 0 // 0-based index of correct option
            }
            Do not include markdown tags like ```json or any trailing text. Just the raw JSON.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json")
        )

        try {
            val response = service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                // Parse the response using Moshi
                val adapter = moshi.adapter(TempleRiddleJson::class.java).lenient()
                val parsed = adapter.fromJson(responseText)
                if (parsed != null && parsed.choices.size >= 4) {
                    return@withContext TempleRiddle(
                        riddle = parsed.riddle,
                        choices = parsed.choices,
                        correctIndex = parsed.correctIndex.coerceIn(0, 3)
                    )
                }
            }
            getFallbackRiddle(level)
        } catch (e: Exception) {
            getFallbackRiddle(level)
        }
    }

    private fun getFallbackRiddle(level: Int): TempleRiddle {
        val fallbacks = listOf(
            TempleRiddle(
                riddle = "أنا جزء منك يزداد كبراً كلما نسيت أكثر، وأذوب في شعلة الصدى الروحي. فما أنا؟",
                choices = listOf(
                    "العتمة السحيقة (The Deep Dark)",
                    "شظية الذكريات (Memory Shard)",
                    "ثقل الفراغ وجوهر النسيان (Void Oblivion)",
                    "تاج الخلود (The Crown of Immortality)"
                ),
                correctIndex = 2
            ),
            TempleRiddle(
                riddle = "أنا سلاح فريد تضربه فيصنع الحياة للأصدقاء والهلاك للغرباء داخل الهاوية، فما أنا؟",
                choices = listOf(
                    "ريشة الطائر (Bird Feather)",
                    "مسمار الضياء المضيء (Glow Nail)",
                    "شعلة الصدى (Echoes Core Flare)",
                    "جرعة الحيوية الفانية (Vitality Potion)"
                ),
                correctIndex = 1
            ),
            TempleRiddle(
                riddle = "أسير بلا ساقين، وأهتف بلا لسان، وأرشد السائح الضال في سراديب النسيان. فما أنا؟",
                choices = listOf(
                    "الروح والصدى الراحل (The Lingering Echo)",
                    "صخور البازلت (Basalt Crags)",
                    "دليل الرماد (Ash Guide Map)",
                    "النجم الذهبي (The Golden Star)"
                ),
                correctIndex = 0
            )
        )
        return fallbacks[(level - 1).coerceIn(0, fallbacks.size - 1)]
    }
}

// Helper container for Moshi parsing
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class TempleRiddleJson(
    val riddle: String,
    val choices: List<String>,
    val correctIndex: Int
)
