package ru.aleshi.letsplaycities.ui.settings

class SettingsItem(defaultValue: Int, val name: String, private val values: Array<String>) {

    fun next() {
        currentValuePosition = ++currentValuePosition % values.size
    }

    fun hasVariants() = values.size < 2

    var currentValuePosition: Int = defaultValue

    val currentValue: String
        get() = if(values.isEmpty()) "" else values[currentValuePosition]
}