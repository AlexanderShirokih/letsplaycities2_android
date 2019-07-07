package ru.aleshi.letsplaycities.base

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

class GamePreferences(context: Context) {

    companion object {
        const val PREFS_NAME = "letsplaycities"
        const val KEY_THEME = "theme"
        const val KEY_DIFF = "gamediff"
        private const val KEY_SCORING = "scoring_sys"
        private const val KEY_TIMER = "timer"
        private const val KEY_SPELLER = "speller"
        private const val KEY_SOUND = "sound"
        private const val KEY_MSG = "rec_msg"
        private const val KEY_UPD_DIC = "dic_upd"
        private const val KEY_NULL = "n"
        private const val KEY_SCR = "scrbkey"
        private const val KEY_BANNED = "banned"

        private val settings_pref_keys =
            arrayOf(
                KEY_NULL,
                KEY_NULL,
                KEY_DIFF,
                KEY_SCORING,
                KEY_TIMER,
                KEY_SPELLER,
                KEY_SOUND,
                KEY_MSG,
                KEY_NULL,
                KEY_NULL,
                KEY_UPD_DIC
            )
        private val settings_defaults = intArrayOf(0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0)
    }

    data class ThemeWithSignature(val theme: String?, val sig: String?) {
        fun isValid() = theme != null && sig != null
    }

    private val prefs: SharedPreferences

    init {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }


    fun putThemeId(stid: Int) {
        putInt(KEY_THEME, stid)
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

    private fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getSettingsValues(): IntArray {
        val array = settings_defaults.copyOf(settings_defaults.size)
        for (i in 0 until settings_defaults.size) {
            array[i] =
                if (settings_pref_keys[i] == KEY_NULL) 0 else prefs.getInt(settings_pref_keys[i], settings_defaults[i])
        }
        return array
    }

    fun putSettingValue(position: Int, value: Int) {
        if (settings_pref_keys[position] != KEY_NULL)
            putInt(settings_pref_keys[position], value)
    }

    fun getScoring(): String? {
        val scrstr = prefs.getString("scrbkey", null)
        return if (scrstr == null) null else String(Base64.decode(scrstr, Base64.DEFAULT))
    }

    fun putScoring(scoring: String) {
        val encoded = Base64.encodeToString(scoring.toByteArray(), Base64.DEFAULT)
        prefs.edit().putString(KEY_SCR, encoded).apply()
    }

    fun getBanned(): String {
        return prefs.getString(KEY_BANNED, "")!!
    }

    fun putBanned(banned: String) {
        prefs.edit().putString(KEY_BANNED, banned).apply()
    }
}