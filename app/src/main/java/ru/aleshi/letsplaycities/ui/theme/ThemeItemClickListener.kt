package ru.aleshi.letsplaycities.ui.theme

interface ThemeItemClickListener {

    fun onUnlock(theme: ThemeListAdapter.NamedTheme)
    fun onSelectTheme(namedTheme: ThemeListAdapter.NamedTheme)
}