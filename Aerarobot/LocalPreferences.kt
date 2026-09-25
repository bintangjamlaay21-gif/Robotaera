package com.aera.robot.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class LocalPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aera_prefs", Context.MODE_PRIVATE)

    var autoSpeak: Boolean
        get() = prefs.getBoolean("auto_speak", true)
        set(value) = prefs.edit().putBoolean("auto_speak", value).apply()

    var wakeWordEnabled: Boolean
        get() = prefs.getBoolean("wake_word", false)
        set(value) = prefs.edit().putBoolean("wake_word", value).apply()

    var memoryEnabled: Boolean
        get() = prefs.getBoolean("memory_enabled", true)
        set(value) = prefs.edit().putBoolean("memory_enabled", value).apply()

    var apiKey: String
        get() = prefs.getString("api_key", "") ?: ""
        set(value) = prefs.edit().putString("api_key", value).apply()

    fun getConversationHistory(): List<Pair<String, String>> {
        if (!memoryEnabled) return emptyList()
        val json = prefs.getString("conversation", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Pair<String, String>>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(obj.getString("role") to obj.getString("text"))
            }
            list.takeLast(20) // keep last 20 turns
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addToConversation(role: String, text: String) {
        if (!memoryEnabled) return
        val history = getConversationHistory().toMutableList()
        history.add(role to text)
        val arr = JSONArray()
        history.takeLast(20).forEach { (r, t) ->
            arr.put(org.json.JSONObject().apply {
                put("role", r)
                put("text", t)
            })
        }
        prefs.edit().putString("conversation", arr.toString()).apply()
    }

    fun clearMemory() {
        prefs.edit().remove("conversation").apply()
    }
}
