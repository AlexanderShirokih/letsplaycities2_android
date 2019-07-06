package ru.aleshi.letsplaycities2.ui.settings

class SettingsItem(defaultValue: Int, val name: String, private val values: Array<String>) {
    fun next() {
        currentValuePosition = ++currentValuePosition % values.size
    }

    fun isEmpty() = values.isEmpty()

    var currentValuePosition: Int = defaultValue

    val currentValue: String
        get() = values[currentValuePosition]
}