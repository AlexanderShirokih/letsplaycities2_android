package ru.aleshi.letsplaycities2.ui

import android.content.Context
import ru.aleshi.letsplaycities2.LPSApplication
import ru.aleshi.letsplaycities2.R
import ru.aleshi.letsplaycities2.base.SignatureChecker
import ru.aleshi.letsplaycities2.base.Theme


object ThemeManager {
    private val themes = arrayOf(
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
        val app = context.applicationContext as LPSApplication
        val theme: Theme = getThemeById(app.gamePreferences.getThemeId(DEFAULT_THEME.stid), app)
        if (theme.isFreeOrAvailable())
            context.setTheme(theme.themeId)
    }

    private fun getThemeById(requestedStid: Int, applicationContext: LPSApplication): Theme {
        for (theme in themes) {
            if (theme.stid == requestedStid)
                checkAvailableFor(theme, applicationContext)
            if (theme.isFreeOrAvailable())
                return theme
        }
        return DEFAULT_THEME
    }

    private fun checkAvailableFor(theme: Theme, app: LPSApplication) {
        if (!theme.isFree()) {
            theme.isAvail =
                app.gamePreferences.getThemeWithSignature(theme).isValid() && SignatureChecker.check(app) == "Y"
        }
    }

}