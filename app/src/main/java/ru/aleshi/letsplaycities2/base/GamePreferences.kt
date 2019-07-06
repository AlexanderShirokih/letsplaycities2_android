package ru.aleshi.letsplaycities2.base

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {

    companion object {
        const val PREFS_NAME = "letsplaycities"
        const val KEY_THEME = "theme"
    }

    data class ThemeWithSignature(val theme: String?, val sig: String?) {
        fun isValid() = theme != null && sig != null
    }

    private val prefs: SharedPreferences

    init {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }


    fun getThemeId(defThemeId: Int): Int {
        return prefs.getInt(KEY_THEME, defThemeId)
    }

    fun getThemeWithSignature(theme: Theme): ThemeWithSignature {
        return ThemeWithSignature(
            prefs.getString("thm:${theme.sku}", null),
            prefs.getString("sig:${theme.sku}", null)
        )
    }

}