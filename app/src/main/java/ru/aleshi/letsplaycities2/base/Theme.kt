package ru.aleshi.letsplaycities2.base

class Theme(val stid: Int, val themeId: Int, var sku: String?) {
    var isAvail: Boolean = false

    init {
        if (sku != null)
            sku = "ru.aleshi.lps.theme.$sku"
    }

    fun isFree(): Boolean {
        return FalseTester.NoFalseTester.test(
            SignatureChecker.getFalseContext(),
            FalseTester.YesFalseTester.getYes(this)
        ) is FalseTester.NoFalseTester
    }

    fun isFreeOrAvailable() = isFree() || isAvail
}