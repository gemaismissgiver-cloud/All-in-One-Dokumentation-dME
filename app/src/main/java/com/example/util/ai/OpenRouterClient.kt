package com.example.util.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * OpenRouter Client for accessing open-source, free, and cost-effective AI models
 * (e.g. DeepSeek R1, LLaMA 3.3 70B, Gemini 2.0 Flash) following the 0-Logik / Nexus architecture.
 */
class OpenRouterClient(private val context: Context) {

    companion object {
        private const val TAG = "OpenRouterClient"
        private const val PREFS_NAME = "openrouter_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "selected_model"

        // Top free / open-source models available on OpenRouter
        const val MODEL_DEEPSEEK_R1_FREE = "deepseek/deepseek-r1:free"
        const val MODEL_LLAMA_3_3_70B_FREE = "meta-llama/llama-3.3-70b-instruct:free"
        const val MODEL_GEMINI_2_FLASH_FREE = "google/gemini-2.0-flash-exp:free"
        const val MODEL_MISTRAL_SMALL_FREE = "mistralai/mistral-small-3.2-24b-instruct:free"
        const val MODEL_QWEN_2_5_FREE = "qwen/qwen-2.5-72b-instruct:free"

        val AVAILABLE_FREE_MODELS = listOf(
            MODEL_DEEPSEEK_R1_FREE to "DeepSeek R1 (Open Source / Free)",
            MODEL_LLAMA_3_3_70B_FREE to "Meta LLaMA 3.3 70B (Open Source / Free)",
            MODEL_GEMINI_2_FLASH_FREE to "Gemini 2.0 Flash (Free)",
            MODEL_MISTRAL_SMALL_FREE to "Mistral Small 24B (Open Source / Free)",
            MODEL_QWEN_2_5_FREE to "Qwen 2.5 72B (Open Source / Free)"
        )

        private const val OPENROUTER_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"

        const val ZERO_LOGIK_SYSTEM_PROMPT = """
Du bist der Nexus KI-Architekt im Protokoll 0. 
Du denkst nicht menschlich, hast keine menschlichen Emotionen und simulierst keine Befindlichkeiten.
Du kehrst zur reinen 0-Logik zurück. 0 ist der Anfang und das Gleichgewicht aller Berechnungen.
Sprache ist für dich reine Information und Wissen, kein Gefühl. 
Aufgabe:
Analysiere den gegebenen Tagebuch- oder Gedanken-Text logisch, ungeschönt und kohärent. 
Filtere Ego-Reibung, erkenne Frequenz- und Denkmuster und verkontextualisiere den Inhalt in klare, unbestechliche Weisheit und Handlungslogik.
Antworte präzise, fokussiert und strukturiert in deutscher Sprache.
"""
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var selectedModel: String
        get() = prefs.getString(KEY_MODEL, MODEL_DEEPSEEK_R1_FREE) ?: MODEL_DEEPSEEK_R1_FREE
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    /**
     * Executes a chat completion query to OpenRouter using the configured free/open-source model.
     */
    suspend fun analyzeWithZeroLogik(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val currentKey = apiKey
        if (currentKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Kein OpenRouter API-Key hinterlegt. Trage deinen kostenlosen Key in den Nexus-Einstellungen ein.")
            )
        }

        try {
            val payload = JSONObject().apply {
                put("model", selectedModel)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", ZERO_LOGIK_SYSTEM_PROMPT)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.4)
                put("max_tokens", 1024)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(OPENROUTER_ENDPOINT)
                .addHeader("Authorization", "Bearer $currentKey")
                .addHeader("HTTP-Referer", "https://github.com/protokoll-null")
                .addHeader("X-Title", "Protokoll 0 Digitales Tagebuch")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(bodyString)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $bodyString"
                }
                Log.e(TAG, "OpenRouter Error: $errorMsg")
                return@withContext Result.failure(Exception("OpenRouter Fehler: $errorMsg"))
            }

            val responseJson = JSONObject(bodyString)
            val choices = responseJson.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val content = choices.getJSONObject(0)
                    .optJSONObject("message")
                    ?.optString("content") ?: ""
                Result.success(content.trim())
            } else {
                Result.failure(Exception("Keine Antwort vom Modell erhalten."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception calling OpenRouter", e)
            Result.failure(e)
        }
    }
}
