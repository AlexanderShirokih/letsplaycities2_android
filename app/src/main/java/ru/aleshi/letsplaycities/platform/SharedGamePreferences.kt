package ru.aleshi.letsplaycities.platform

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Base64
import ru.aleshi.letsplaycities.base.GamePreferences
import javax.inject.Inject

class SharedGamePreferences @Inject constructor(val prefs: SharedPreferences) : GamePreferences() {

    /**
     * Gets a boolean value by key from preferences.
     * @param key parameter key
     * @param defaultValue default value if parameter with given [key] if not present.
     * @return value associated with [key] or [defaultValue] it key is not present
     */
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    /**
     * Puts a boolean value by key to preferences.
     * @param key parameter key
     * @param value value to be saved.
     */
    override fun putBoolean(key: String, value: Boolean) =
        prefs.edit().putBoolean(key, value).apply()

    /**
     * Gets a long value by key from preferences.
     * @param key parameter key
     * @param defaultValue default value if parameter with given [key] if not present.
     * @return value associated with [key] or [defaultValue] it key is not present
     */
    override fun getLong(key: String, defaultValue: Long): Long = prefs.getLong(key, defaultValue)

    /**
     * Puts a long value by key to preferences.
     * @param key parameter key
     * @param value value to be saved.
     */
    override fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()

    /**
     * Gets integer value by key from preferences.
     * @param key parameter key
     * @param defaultValue default value if parameter with given [key] if not present.
     * @return value associated with [key] or [defaultValue] it key is not present
     */
    override fun getInt(key: String, defaultValue: Int): Int = prefs.getInt(key, defaultValue)

    /**
     * Puts an integer value by key to preferences.
     * @param key parameter key
     * @param value value to be saved.
     */
    override fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()

    /**
     * Gets a string value by key from preferences.
     * @param key parameter key
     * @param defaultValue default value if parameter with given [key] if not present.
     * @return value associated with [key] or [defaultValue] it key is not present
     */
    override fun getString(key: String, defaultValue: String?): String? =
        prefs.getString(key, defaultValue)

    /**
     * Puts a string value by key to preferences.
     * @param key parameter key
     * @param value value to be saved.
     */
    override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()

    /**
     * Gets a preferences string decoded from Base64 or `null` if it's present.
     * @param key parameter key
     */
    override fun getBase64(key: String): String? =
        prefs.getString(key, null)?.run { String(Base64.decode(this, Base64.DEFAULT)) }

    /**
     * Encodes string to Base64 and store it to preferences.
     * @param key parameter key
     * @param value to be encoded and saved to prefs
     */
    override fun putBase64(key: String, value: String) =
        putString(key, Base64.encodeToString(key.toByteArray(), Base64.DEFAULT))

    /**
     * Opens editor for putting parameters in a single batch
     * @param commit if `true` value will saved immediately (synchronously), `false` - applied
     * asynchronously.
     */
    override fun edit(commit: Boolean, editor: Editor.() -> Unit) {
        @SuppressLint("CommitPrefEdits")
        val preferencesEditor = SharedPreferencesEditor(prefs.edit())
        editor(preferencesEditor)
        preferencesEditor.flush(commit)
    }

    internal class SharedPreferencesEditor(private val editor: SharedPreferences.Editor) : Editor {
        override fun putString(key: String, value: String?) {
            editor.putString(key, value)
        }

        override fun putInt(key: String, value: Int) {
            editor.putInt(key, value)
        }

        override fun remove(key: String) {
            editor.remove(key)
        }

        fun flush(sync: Boolean) {
            if (sync)
                editor.commit()
            else
                editor.apply()
        }

    }

}