package ru.aleshi.letsplaycities.ui.settings

class SettingsItem(defaultValue: Int, val name: String, private val values: Array<String>) {
    companion object {

        var disabledVariantName: String = ""

    }

    fun next() {
        currentValuePosition = ++currentValuePosition % values.size
    }

    fun hasAdvancedVariants() = values.size > 2

    fun isEnabled() = disabledVariantName != currentValue

    fun canBeEnabled() = values.any { disabledVariantName == it }

    var currentValuePosition: Int = defaultValue

    val currentValue: String
        get() = if (values.isEmpty()) "" else values[currentValuePosition]
}