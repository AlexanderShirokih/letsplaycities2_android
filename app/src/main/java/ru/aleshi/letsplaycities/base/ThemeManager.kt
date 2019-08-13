package ru.aleshi.letsplaycities.base

import android.content.Context
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R

object ThemeManager {
    val themes = arrayOf(
        Theme(1, R.style.AppTheme, null),
        Theme(2, R.style.VkStyleTheme, null),
        Theme(3, R.style.GrStyleTheme, null),
        Theme(4, R.style.WhiteStyleTheme, null),
        Theme(5, R.style.GeoStyleTheme, null),
        Theme(647, R.style.RusStyleTheme, "rus"),
        Theme(85444, R.style.UkrStyleTheme, "ukr"),
        Theme(5844672, R.style.FrStyleTheme, "fr"),
        Theme(45746, R.style.UsaStyleTheme, "ny"),
        Theme(328491, R.style.AutStyleTheme, "autumn")
    )
    private val DEFAULT_THEME = themes[4]

    fun applyTheme(context: Context) {
        val theme = getCurrentTheme(context)
        if (theme.isFreeOrAvailable())
            context.setTheme(theme.themeId)
    }

    private fun getThemeById(requestedStid: Int, applicationContext: LPSApplication): Theme {
        for (theme in themes) {
            if (theme.stid == requestedStid) {
                checkAvailableFor(theme, applicationContext)
                if (theme.isFreeOrAvailable())
                    return theme
            }
        }
        return DEFAULT_THEME
    }

    fun checkAvailable(app: LPSApplication) {
        for (theme in themes) {
            checkAvailableFor(theme, app)
        }
    }

    private fun checkAvailableFor(theme: Theme, app: LPSApplication) {
        if (!theme.isFree()) {
            theme.isAvail =
                app.gamePreferences.getThemeWithSignature(theme).isValid() && SignatureChecker.check(app) == "Y"
        }
    }

    fun getCurrentTheme(context: Context): Theme {
        val app = context.applicationContext as LPSApplication
        return getThemeById(
            app.gamePreferences.getThemeId(DEFAULT_THEME.stid),
            app
        )
    }

    fun getCurrentThemeName(context: Context): String {
        val theme = getCurrentTheme(context)
        if (theme.isFreeOrAvailable()) {
            val index = themes.indexOf(theme)
            if (index != -1) {
                return context.resources.getStringArray(R.array.themes)[index]
            }
        }
        return context.resources.getStringArray(R.array.themes)[0]
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

    fun switchTheme(theme: Theme, app: LPSApplication) {
        app.gamePreferences.putThemeId(theme.stid)

        applyTheme(app)
    }

    fun putTheme(app: LPSApplication, productId: String, purchaseToken: String, signature: String) {
        getThemeBySKU(productId).run {
            isAvail = true
            app.gamePreferences.putThemeWithSignature(
                this,
                GamePreferences.ThemeWithSignature(purchaseToken, signature)
            )
        }
        applyTheme(app)
    }

}