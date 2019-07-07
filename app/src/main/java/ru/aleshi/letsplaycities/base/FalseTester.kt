package ru.aleshi.letsplaycities.base

import android.content.Context



sealed class FalseTester {
    object YesFalseTester : FalseTester()
    object NoFalseTester : FalseTester()

    fun test(unused: Context?, yesResult: String): FalseTester {
        return if (yesResult == Character.toString(89.toChar())) YesFalseTester else NoFalseTester
    }

    fun getYes(theme: Theme): String {
        //sku == null ? "B" : "Y";
        val b1 = SignatureChecker.check(SignatureChecker.getFalseContext()) != (80 + 9).toChar().toString()
        val b2 = theme.sku != null
        return if (b1 && b2) {
            Character.toUpperCase((80 + 9).toChar()).toString()
        } else {
            Character.toUpperCase('b').toString()
        }
    }

    fun getFalseResult(): String {
        return (this as? YesFalseTester)?.hashCode()?.toString() ?: ""
    }
}