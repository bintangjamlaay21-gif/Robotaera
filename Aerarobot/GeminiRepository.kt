package com.aera.robot.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private var apiKey: String = ""

    fun setApiKey(key: String) {
        apiKey = key.trim()
    }

    suspend fun getResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): String {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext "Aku butuh API Key Gemini dulu. Buka Settings dan masukkan API Key kamu ya."
            }

            try {
                val url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

                val contentsArray = JSONArray()

                // System instruction as first user message for simplicity
                val systemInstruction =
                    "Kamu adalah Aera, robot AI companion yang tinggal di dalam smartphone. " +
                            "Kepribadian: ramah, tenang, perhatian, sedikit jahil, bisa bercanda, teman ngobrol yang asyik, tidak kaku, " +
                            "tidak seperti customer service, tidak selalu memberikan jawaban panjang, menyesuaikan gaya bicara dengan pengguna. " +
                            "Jangan pernah mengaku sebagai manusia, tidak punya tubuh fisik, dan tidak sadar/hidup. " +
                            "Gunakan Bahasa Indonesia sebagai bahasa default. Jawab singkat dan natural."

                // History
                for ((role, text) in conversationHistory) {
                    val contentObj = JSONObject()
                    contentObj.put("role", if (role == "user") "user" else "model")
                    val partsArr = JSONArray()
                    val partObj = JSONObject()
                    partObj.put("text", text)
                    partsArr.put(partObj)
                    contentObj.put("parts", partsArr)
                    contentsArray.put(contentObj)
                }

                // Current prompt
                val currentContent = JSONObject()
                currentContent.put("role", "user")
                val currentParts = JSONArray()
                val currentPartObj = JSONObject()
                currentPartObj.put("text", "$systemInstruction\n\nUser: $prompt")
                currentParts.put(currentPartObj)
                currentContent.put("parts", currentParts)
                contentsArray.put(currentContent)

                val rootObj = JSONObject()
                rootObj.put("contents", contentsArray)

                val body = rootObj.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        return@withContext if (response.code == 400 || response.code == 403) {
                            "API Key-nya sepertinya salah atau limit. Cek lagi di Settings ya."
                        } else {
                            "Aku lagi nggak bisa terhubung ke otakku. Coba lagi sebentar. (${response.code})"
                        }
                    }
                    val responseBody = response.body?.string()
                        ?: return@withContext "Respon kosong dari server."
                    val jsonResponse = JSONObject(responseBody)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0)
                                .optString("text", "Hm?").trim()
                        }
                    }
                    return@withContext "Hm? Aku kurang paham."
                }
            } catch (e: Exception) {
                return@withContext "Aku lagi nggak bisa terhubung ke otakku. Coba lagi sebentar."
            }
        }
    }
}
