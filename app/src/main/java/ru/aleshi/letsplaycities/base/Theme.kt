package ru.aleshi.letsplaycities.base

class Theme(val stid: Int, val themeId: Int, var sku: String?) {
    var isAvail: Boolean = false

    init {
        if (sku != null)
            sku = "ru.aleshi.lps.theme.$sku"
    }

    fun isFree() = sku == null

    fun isFreeOrAvailable() = isFree() || isAvail
}