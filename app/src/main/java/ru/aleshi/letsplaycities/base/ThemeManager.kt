package ru.aleshi.letsplaycities.base

import ru.aleshi.letsplaycities.R

/**
 * Manages game themes (styles)
 */
object ThemeManager {

    private const val KEY_THEME = "token"

    /**
     * Wrapper data class for purchased theme
     */
    data class ThemeWithSignature(val purchaseToken: String?, val sig: String?) {
        fun isValid() = purchaseToken != null && sig != null
    }

    /**
     * List of the game themes
     */
    val themes = arrayOf(
        Theme(1, R.style.AppTheme),
        Theme(2, R.style.VkStyleTheme),
        Theme(3, R.style.GrStyleTheme),
        Theme(4, R.style.WhiteStyleTheme),
        Theme(5, R.style.GeoStyleTheme),
        Theme(6, R.style.DarkStyleTheme),
        Theme(647, R.style.RusStyleTheme, "rus"),
        Theme(85444, R.style.UkrStyleTheme, "ukr"),
        Theme(5844672, R.style.FrStyleTheme, "fr"),
        Theme(45746, R.style.UsaStyleTheme, "ny"),
        Theme(328491, R.style.AutStyleTheme, "autumn")
    )

    /**
     * Theme which applies by default if
     */
    private val DEFAULT_THEME = themes[4]

    /**
     * Returns [Theme] by [requestedStid] or [DEFAULT_THEME] if requested theme not found
     * @param requestedStid theme id
     */
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

    /**
     * Checks all game themes availability ([Theme.isAvail])
     */
    fun checkAvailable(prefs: GamePreferences) {
        for (theme in themes) {
            checkAvailableFor(theme, prefs)
        }
    }

    /**
     * If [theme] is not free, updates [Theme.isAvail] by validating theme signature
     */
    private fun checkAvailableFor(theme: Theme, prefs: GamePreferences) {
        if (!theme.isFree()) {
            theme.isAvail = getThemeWithSignature(prefs, theme).isValid()
        }
    }

    /**
     * Returns current theme from game preferences
     */
    fun getCurrentTheme(prefs: GamePreferences): Theme {
        return getThemeById(
            prefs.getInt(KEY_THEME, DEFAULT_THEME.stid),
            prefs
        )
    }

    /**
     * Returns current theme name from [themeNames] array or first theme name `themeNames[0]` if
     * current theme isn't available.
     */
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

    /**
     * Returns [Theme] from [productId]
     * @throws IllegalStateException if there are no theme with [productId]
     */
    private fun getThemeBySKU(productId: String): Theme {
        for (t in themes) {
            if (productId == t.sku)
                return t
        }
        throw IllegalStateException("Cannot find token by sku $productId")
    }

    /**
     * Anti-cheat method. Asserts that 8th theme isn't free. Should be inverted!
     */
    fun test2(): Boolean {
        return themes[7].isFree()
    }

    /**
     * Returns SKU's list from all non-free themes.
     */
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

    /**
     * Saves purchased theme to game preferences and make it available
     */
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
            putString("thm:${theme.sku}", themeWithSignature.purchaseToken)
            putString("sig:${theme.sku}", themeWithSignature.sig)
            putInt(KEY_THEME, theme.stid)
        }
    }

}