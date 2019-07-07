package ru.aleshi.letsplaycities.ui.theme

interface ThemeItemClickListener {

    fun onUnlock(theme: ThemeListAdapter.NamedTheme)
    fun onShowPreview(theme: ThemeListAdapter.NamedTheme, position: Int)
    fun onSelectTheme(namedTheme: ThemeListAdapter.NamedTheme)
}