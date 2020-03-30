package ru.aleshi.letsplaycities.ui.theme

/**
 * Interface for handling theme item clicks
 */
interface ThemeItemClickListener {

    /**
     * Called when user clicks the purchase button
     */
    fun onUnlock(theme: ThemeListAdapter.NamedTheme)

    /**
     * Called when user clicks on theme item
     */
    fun onSelectTheme(namedTheme: ThemeListAdapter.NamedTheme)
}