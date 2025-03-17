package com.example.delta

import org.json.JSONObject
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
// Adjust package name if needed


class GeminiHelper {
    private val API_KEY = BuildConfig.GEMINI_API_KEY // Move API key to BuildConfig
    private val client = OkHttpClient()

    fun getGeminiResponse(inputText: String, callback: (String) -> Unit) {
        val json = """
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "$inputText"
                }
              ]
            }
          ],
          "generationConfig": {
            "maxOutputTokens": 50,
            "temperature": 0.3
          }
        }
        """.trimIndent()


        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)
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
                        ?.optString("text", "🤖 No valid response from Gemini")

                    callback(text ?: "🤖 No response from Gemini")
                } catch (e: Exception) {
                    callback("⚠️ Parsing Error: Unable to process AI response.")
                }
            }
        })
    }
}
