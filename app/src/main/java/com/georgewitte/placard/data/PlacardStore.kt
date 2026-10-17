package com.georgewitte.placard.data

import android.content.Context
import com.georgewitte.placard.logic.Phrases
import org.json.JSONArray
import org.json.JSONException

/** Everything Placard remembers, kept on the device in SharedPreferences. */
class PlacardStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("placard", Context.MODE_PRIVATE)

    var phrases: List<String>
        get() {
            val raw = prefs.getString(KEY_PHRASES, null) ?: return Phrases.DEFAULTS
            return try {
                val array = JSONArray(raw)
                List(array.length()) { array.getString(it) }
            } catch (e: JSONException) {
                Phrases.DEFAULTS
            }
        }
        set(value) {
            val array = JSONArray()
            value.forEach { array.put(it) }
            prefs.edit().putString(KEY_PHRASES, array.toString()).apply()
        }

    var styleIndex: Int
        get() = prefs.getInt(KEY_STYLE, 0)
        set(value) = prefs.edit().putInt(KEY_STYLE, value).apply()

    var ticker: Boolean
        get() = prefs.getBoolean(KEY_TICKER, false)
        set(value) = prefs.edit().putBoolean(KEY_TICKER, value).apply()

    var maxBrightness: Boolean
        get() = prefs.getBoolean(KEY_BRIGHTNESS, true)
        set(value) = prefs.edit().putBoolean(KEY_BRIGHTNESS, value).apply()

    var draft: String
        get() = prefs.getString(KEY_DRAFT, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_DRAFT, value).apply()

    private companion object {
        const val KEY_PHRASES = "phrases_v1"
        const val KEY_STYLE = "style"
        const val KEY_TICKER = "ticker"
        const val KEY_BRIGHTNESS = "max_brightness"
        const val KEY_DRAFT = "draft"
    }
}
