package ru.aleshi.letsplaycities.base

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit

class GamePreferences(context: Context) {

    companion object {
        const val PREFS_NAME = "letsplaycities"

        const val KEY_THEME = "token"
        const val KEY_DIFF = "gamediff"
        private const val KEY_SHOW_CHANGE_MODE_DIALOG = "_show_chm"
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

    data class ThemeWithSignature(val token: String?, val sig: String?) {
        fun isValid() = token != null && sig != null
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

    fun putThemeWithSignature(theme: Theme, themeWithSignature: ThemeWithSignature) {
        prefs.edit()
            .putString("thm:${theme.sku}", themeWithSignature.token)
            .putString("sig:${theme.sku}", themeWithSignature.sig)
            .putInt(KEY_THEME, theme.stid)
            .apply()
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
        val scrstr = prefs.getString(KEY_SCR, null)
        return if (scrstr == null) null else String(Base64.decode(scrstr, Base64.DEFAULT))
    }

    fun getCurrentScoringType(): Int {
        return prefs.getInt(KEY_SCORING, 0)
    }

    fun setCurrentScoringType(id: Int) {
        putInt(KEY_SCORING, id)
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

    fun getString(key: String, def: String?): String? {
        return prefs.getString(key, def)
    }

    fun getInt(key: String, def: Int): Int {
        return prefs.getInt(key, def)
    }

    fun edit(): SharedPreferences.Editor {
        return prefs.edit()
    }

    fun isChangeModeDialogRequested(): Boolean {
        val shown = prefs.getBoolean(KEY_SHOW_CHANGE_MODE_DIALOG, false)
        prefs.edit().putBoolean(KEY_SHOW_CHANGE_MODE_DIALOG, true).apply()
        return !shown
    }

    fun isLoggedFromAnySN(): Boolean {
        return prefs.getString("sn_uid", null) != null
    }

    fun logout() {
        prefs.edit {
            remove("avatar_path")
            remove("sn_login")
            remove("sn_uid")
            remove("sn_name")
            remove("hash")
            remove("user_id")
            remove("acc_tkn")
        }
    }

    fun getLogin(): String = prefs.getString("sn_login", "Player")!!

    fun removeAvatarPath() {
        prefs.edit().remove("avatar_path").apply()
    }

    fun setAvatarPath(avatar: String) {
        prefs.edit().putString("avatar_path", avatar).apply()
    }

    fun getAvatarPath(): String? {
        return prefs.getString("avatar_path", null)
    }

    fun getLastNativeLogin(): String? {
        return prefs.getString("last_login", null)
    }

    fun updateLastNativeLogin(login: String) {
        prefs.edit {
            putString("last_login", login)
        }
    }

    fun getLastAvatarUri(): String? {
        return prefs.getString("last_avatar_uri", null)
    }

    fun updateLastAvatarUri(uri: String) {
        prefs.edit {
            putString("last_avatar_uri", uri)
        }
    }

    fun canReceiveMessages(): Boolean {
        return prefs.getInt(KEY_MSG, 1) != 0
    }

}