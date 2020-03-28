package ru.aleshi.letsplaycities.base

import java.net.URI

abstract class GamePreferences {

    /**
     * Editor interface for editing preferences in a single batch
     */
    interface Editor {
        /**
         * Puts string [value] associated with [key]
         */
        fun putString(key: String, value: String?)

        /**
         * Puts integer [value] associated with [key]
         */
        fun putInt(key: String, value: Int)

        /**
         * Removes value associated with [key] from preferences
         */
        fun remove(key: String)
    }

    companion object {
        private const val KEY_DIFF = "gamediff"
        private const val F_LAUNCH = "first_launch"
        private const val LAUNCHES_TO_RATE = "l2r2"
        private const val KEY_SHOW_CHANGE_MODE_DIALOG = "_show_chm"
        private const val KEY_SCORING = "scoring_sys"
        private const val KEY_TIMER = "timer"
        private const val KEY_SPELLER = "speller"
        private const val KEY_SOUND = "sound"
        private const val KEY_MSG = "rec_msg"
        private const val KEY_UPD_DIC = "dic_upd"
        private const val KEY_NULL = "n"
        private const val KEY_SCR = "scrbkey"
        private const val KEY_LAST_UPDATE = "last_upd_date"

        private val settings_pref_keys =
            arrayOf(
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
        private val settings_defaults = intArrayOf(0, 0, 0, 0, 1, 1, 1, 0, 0, 0)
    }

    /**
     * Gets a boolean value by key from preferences.
     * @param key parameter key
     * @param defaultValue default value if parameter with given [key] if not present.
     * @return value associated with [key] or [defaultValue] it key is not present
     */
    abstract fun getBoolean(key: String, defaultValue: Boolean): Boolean

    /**
     * Puts a boolean value by key to preferences.
     * @param key parameter key
     * @param value value to be saved.
     */
    abstract fun putBoolean(key: String, value: Boolean)

    /**
     * Gets a long value by key from preferences.
     * @param key parameter key
     * @param defaultValue default value if parameter with given [key] if not present.
     * @return value associated with [key] or [defaultValue] it key is not present
     */
    abstract fun getLong(key: String, defaultValue: Long): Long

    /**
     * Puts a long value by key to preferences.
     * @param key parameter key
     * @param value value to be saved.
     */
    abstract fun putLong(key: String, value: Long)

    /**
     * Gets an integer value by key from preferences.
     * @param key parameter key
     * @param defaultValue default value if parameter with given [key] if not present.
     * @return value associated with [key] or [defaultValue] it key is not present
     */
    abstract fun getInt(key: String, defaultValue: Int): Int

    /**
     * Puts an integer value by key to preferences.
     * @param key parameter key
     * @param value value to be saved.
     */
    abstract fun putInt(key: String, value: Int)

    /**
     * Gets a string value by key from preferences.
     * @param key parameter key
     * @param defaultValue default value if parameter with given [key] if not present.
     * @return value associated with [key] or [defaultValue] it key is not present
     */
    abstract fun getString(key: String, defaultValue: String?): String?

    /**
     * Puts a string value by key to preferences.
     * @param key parameter key
     * @param value value to be saved.
     */
    abstract fun putString(key: String, value: String)

    /**
     * Gets a preferences string decoded from Base64 or `null` if it's present.
     * @param key parameter key
     */
    abstract fun getBase64(key: String): String?

    /**
     * Encodes string to Base64 and store it to preferences.
     * @param key parameter key
     * @param value to be encoded and saved to prefs
     */
    abstract fun putBase64(key: String, value: String)

    /**
     * Opens [Editor] for putting parameters in a single batch
     * @param commit if `true` value will saved immediately (synchronously), `false` - applied
     * asynchronously.
     */
    abstract fun edit(commit: Boolean = false, editor: Editor.() -> Unit)

    /**
     * Loads settings values from preferences
     */
    fun getSettingsValues() =
        settings_defaults.copyOf(settings_defaults.size).mapIndexed { i: Int, value: Int ->
            if (settings_pref_keys[i] == KEY_NULL) 0
            else getInt(settings_pref_keys[i], value)
        }

    /**
     * Saves settings values to preferences
     * @param position setting possition
     * @param value value to be saved
     */
    fun putSettingValue(position: Int, value: Int) {
        if (settings_pref_keys[position] != KEY_NULL)
            putInt(settings_pref_keys[position], value)
    }

    /**
     * Returns decoded scoring string or `null` if it's not present.
     */
    fun getScoring(): String? = getBase64(KEY_SCR)

    /**
     * Saves scoring to preferences
     * @param scoring scoring to be saved
     */
    fun putScoring(scoring: String) = putBase64(KEY_SCR, scoring)

    /**
     * Returns current scoring type
     */
    fun getCurrentScoringType(): Int = getInt(KEY_SCORING, 0)

    /**
     * Sets current scoring type
     * @param id scoring type
     */
    fun setCurrentScoringType(id: Int) = putInt(KEY_SCORING, id)

    /**
     * Returns `true` if change mode dialog is not triggered before, since app was installed
     */
    fun isChangeModeDialogRequested(): Boolean {
        val shown = getBoolean(KEY_SHOW_CHANGE_MODE_DIALOG, false)
        putBoolean(KEY_SHOW_CHANGE_MODE_DIALOG, true)
        return !shown
    }

    /**
     * Returns `true` if player logged with any social network
     */
    fun isLoggedIn(): Boolean {
        return getInt("user_id", 0) != 0
    }

    fun logout() {
        edit {
            remove("sn_login")
            remove("sn_name")
            remove("user_id")
            remove("acc_tkn")
            remove("commited")
            remove("pic_hash")
            // Legacy data
            remove("sn_uid")
            remove("hash")
            remove("avatar_path")
        }
    }

    /**
     * Removes avatar path from preferences
     */
    fun removeAvatarPath() {
        edit { remove("last_avatar_uri") }
    }

    /**
     * Login used for native authorization
     */
    var lastNativeLogin: String
        get() = getString("last_login", "")!!
        set(login) = putString("last_login", login)

    /**
     *
     */
    var lastAvatarUri: URI?
        get() = getString("last_avatar_uri", null)?.run { URI.create(this) }
        set(uri) = edit(true) { putString("last_avatar_uri", uri.toString()) }

    /**
     * Players picture MD5 hash
     */
    var pictureHash: String?
        get() = getString("pic_hash", null)
        set(hash) = edit(true) { putString("pic_hash", hash) }

    /**
     * Returns `true` if player allows to receive messages from other users.
     */
    fun canReceiveMessages(): Boolean {
        return getInt(KEY_MSG, 1) != 0
    }

    /**
     * Returns `true` if words auto-correction is enabled
     */
    fun isCorrectionEnabled(): Boolean {
        return getInt(KEY_SPELLER, 1) != 0
    }

    /**
     * Returns `true` if sound effect enabled
     */
    fun isSoundEnabled(): Boolean {
        return getInt(KEY_SOUND, 1) != 0
    }

    /**
     * Checks when RateDialog should be launched and calls
     * [callback] when it should happen.
     */
    fun checkForRateDialogLaunch(callback: () -> Unit) {
        var launchesToRate = getInt(LAUNCHES_TO_RATE, -1)
        if (launchesToRate == -1) {
            launchesToRate = (2..5).random()
        }
        when {
            launchesToRate > 0 -> launchesToRate--
            launchesToRate == 0 -> {
                launchesToRate = -2
                callback()
            }
            else -> return
        }
        putInt(LAUNCHES_TO_RATE, launchesToRate)
    }

    /**
     * Gets current time limit settings for local games
     */
    fun getTimeLimit(): Long {
        val timer = getInt(KEY_TIMER, 0)
        return if (timer == 0) 0L else (timer * 2 - 1) * 60L
    }

    /**
     * Gets dictionary update period
     */
    fun getDictionaryUpdatePeriod(): Long {
        return when (getInt(KEY_UPD_DIC, 0)) {
            0 -> 3L
            1 -> 24L
            else -> 0L
        }
    }

    /**
     * Last dictionary update data
     */
    var dictionaryUpdateDate: Long
        get() = getLong(KEY_LAST_UPDATE, 0)
        set(time) = putLong(KEY_LAST_UPDATE, time)

    /**
     * Returns `true` is only when app starts for the first time
     */
    fun isFirstLaunch(): Boolean {
        val isFirst = getBoolean(F_LAUNCH, true)
        if (isFirst)
            putBoolean(F_LAUNCH, false)
        return isFirst
    }

    /**
     * Gets current dictionary difficulty in range [1..4]
     */
    fun getDifficulty(): Int = getInt(KEY_DIFF, 0) + 1

}