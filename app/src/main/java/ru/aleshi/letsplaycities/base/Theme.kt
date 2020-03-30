package ru.aleshi.letsplaycities.base

/**
 * Game theme.
 * @param stid theme ID, used internally, something like theme primary key
 * @param themeId android style ID
 * @param sku theme SKU for purchasing. For free themes `null`
 */
class Theme(val stid: Int, val themeId: Int, var sku: String? = null) {

    /**
     * `true` then theme is purchased
     */
    var isAvail: Boolean = false

    init {
        if (sku != null)
            sku = "ru.aleshi.lps.theme.$sku"
    }

    /**
     * Returns `true` is theme is free
     */
    fun isFree() = sku == null

    /**
     * Returns `true` if theme can be applied (it is free or purchased)
     */
    fun isFreeOrAvailable() = isFree() || isAvail
}