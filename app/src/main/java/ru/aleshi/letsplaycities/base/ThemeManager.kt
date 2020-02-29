package ru.aleshi.letsplaycities.base

import ru.aleshi.letsplaycities.R

object ThemeManager {

    private const val KEY_THEME = "token"

    data class ThemeWithSignature(val token: String?, val sig: String?) {
        fun isValid() = token != null && sig != null
    }

    val themes = arrayOf(
        Theme(1, R.style.AppTheme, null),
        Theme(2, R.style.VkStyleTheme, null),
        Theme(3, R.style.GrStyleTheme, null),
        Theme(4, R.style.WhiteStyleTheme, null),
        Theme(5, R.style.GeoStyleTheme, null),
        Theme(6, R.style.DarkStyleTheme, null),
        Theme(647, R.style.RusStyleTheme, "rus"),
        Theme(85444, R.style.UkrStyleTheme, "ukr"),
        Theme(5844672, R.style.FrStyleTheme, "fr"),
        Theme(45746, R.style.UsaStyleTheme, "ny"),
        Theme(328491, R.style.AutStyleTheme, "autumn")
    )
    private val DEFAULT_THEME = themes[4]

    private fun getThemeById(requestedStid: Int, prefs: GamePreferences): Theme {
        for (theme in themes) {
            if (theme.stid == requestedStid) {
                checkAvailableFor(theme, prefs)
                if (theme.isFreeOrAvailable())
                    return theme
            }
        }
        return DEFAULT_THEME
    }

    fun checkAvailable(prefs: GamePreferences) {
        for (theme in themes) {
            checkAvailableFor(theme, prefs)
        }
    }

    private fun checkAvailableFor(theme: Theme, prefs: GamePreferences) {
        if (!theme.isFree()) {
            theme.isAvail = getThemeWithSignature(prefs, theme).isValid()
        }
    }

    fun getCurrentTheme(prefs: GamePreferences): Theme {
        return getThemeById(
            prefs.getInt(KEY_THEME, DEFAULT_THEME.stid),
            prefs
        )
    }

    fun getCurrentThemeName(prefs: GamePreferences, themeNames: Array<String>): String {
        val theme = getCurrentTheme(prefs)
        if (theme.isFreeOrAvailable()) {
            val index = themes.indexOf(theme)
            if (index != -1) {
                return themeNames[index]
            }
        }
        return themeNames[0]
    }

    private fun getThemeBySKU(productId: String): Theme {
        for (t in themes) {
            if (productId == t.sku)
                return t
        }
        throw IllegalStateException("Cannot find token by sku $productId")
    }

    fun test2(): Boolean {
        return themes[7].isFree()
    }

    fun getSkusList(): List<String> {
        return themes.mapNotNull { it.sku }.toList()
    }


    /**
     * Saves current theme id to preferences.
     * @param prefs application preferences instance
     * @param theme theme to be saved
     */
    fun saveCurrentTheme(prefs: GamePreferences, theme: Theme) {
        prefs.putInt(KEY_THEME, theme.stid)
    }

    fun putTheme(
        prefs: GamePreferences,
        productId: String,
        purchaseToken: String,
        signature: String
    ) {
        getThemeBySKU(productId).run {
            isAvail = true
            putThemeWithSignature(
                prefs,
                this,
                ThemeWithSignature(purchaseToken, signature)
            )
        }
    }

    /**
     * Loads theme [theme] from preferences
     * @param theme theme to be loaded
     * @return loaded [ThemeWithSignature]
     */
    private fun getThemeWithSignature(prefs: GamePreferences, theme: Theme): ThemeWithSignature {
        return ThemeWithSignature(
            prefs.getString("thm:${theme.sku}", null),
            prefs.getString("sig:${theme.sku}", null)
        )
    }

    /**
     * Saves theme [theme] to preferences.
     * @param theme theme to be saved
     * @param themeWithSignature [ThemeWithSignature] instance to be saved
     */
    private fun putThemeWithSignature(
        prefs: GamePreferences,
        theme: Theme,
        themeWithSignature: ThemeWithSignature
    ) {
        prefs.edit {
            putString("thm:${theme.sku}", themeWithSignature.token)
            putString("sig:${theme.sku}", themeWithSignature.sig)
            putInt(KEY_THEME, theme.stid)
        }
    }

}