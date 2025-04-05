package com.example.delta

import android.util.Log
import org.json.JSONObject
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray

class GeminiHelper {
    private val API_KEY = BuildConfig.GEMINI_API_KEY
    private val client = OkHttpClient()

    fun getGeminiResponse(prompt: String, history: List<Map<String, String>>, callback: (String) -> Unit) {
        val contentArray = JSONArray()

        // Instead of system role, insert instruction in the first user message
        val firstUserMessage =  "Respond in the language the user is using. Keep replies concise, natural, and relevant to the context."

        // Include only the last 10 messages for context
        val trimmedHistory = history.takeLast(10)

        for ((index, entry) in trimmedHistory.withIndex()) {
            val role = if (entry["role"] == "assistant") "model" else "user"
            var text = entry["content"] ?: ""

            // Attach the instruction to the first user message
            if (role == "user" && index == 0) {
                text = "$firstUserMessage\n\n$text"
            }

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", text))
            }

            contentArray.put(JSONObject().put("role", role).put("parts", partsArray))
        }

        // Add latest user prompt
        val userParts = JSONArray().apply {
            put(JSONObject().put("text", prompt))
        }
        contentArray.put(JSONObject().put("role", "user").put("parts", userParts))

        // Dynamically adjust max tokens based on input length
        val maxTokens = if (prompt.length < 50) 200 else 300

        val requestBodyJson = JSONObject().apply {
            put("contents", contentArray)
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", maxTokens)
                put("temperature", 0.7)
            })
        }
        Log.d("GeminiDebug", "Request JSON: $requestBodyJson")

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$API_KEY")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("⚠️ Network Error: Please check your internet connection.")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback("❌ Error ${response.code}: ${response.message}")
                    return
                }

                val responseData = response.body?.string()
                try {
                    val jsonObject = JSONObject(responseData ?: "{}")
                    val candidates = jsonObject.optJSONArray("candidates")

                    val text = candidates?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text", "🤖 No valid response from Gemini") ?: "🤖 No response from Gemini"

                    callback(text)
                } catch (e: Exception) {
                    callback("⚠️ Parsing Error: Unable to process AI response.")
                }
            }
        })
    }
}
