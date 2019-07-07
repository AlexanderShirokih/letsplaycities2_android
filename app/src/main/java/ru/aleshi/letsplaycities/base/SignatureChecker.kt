package ru.aleshi.letsplaycities.base

import android.content.Context

object SignatureChecker {

    fun check(context: Context?): String {
        var sigChk = "B"

        if (context != null) {

            if (checkSignature() && !checkLuckyPatcher(context)) {
                sigChk = "Y"
            }
        }
        return sigChk
    }

    private fun checkSignature() = true

    private fun checkLuckyPatcher(context: Context): Boolean {
        var s = "\u0084|}{wr<{}q\u0080svq\u0082o~\u0087yq\u0083z<}srw"
        if (packageExists(context, s)) {
            return true
        }

        s = "\u0083~zsvq<{}qvq\u0082o~\u0087yqoz<\u0081"

        if (packageExists(context, s)) {
            return true
        }
        s = "w}\u0080r|o<{}qYQOZ<sqw\u0084\u0080sau|wzzwP~~O|W<u|wzzwp<u|wr|s\u0084<r"
        if (packageExists(context, s)) {
            return true
        }

        s = "w}\u0080r|o<{}q\\W]Q<sqw\u0084\u0080sau|wzzwP~~O|W<u|wzzwp<u|wr|s\u0084<r"
        return packageExists(context, s)

    }

    private fun packageExists(context: Context, packageName: String): Boolean {
        try {
            // No need really to test for null, if the package does not
            // exist it will really rise an exception. but in case Google
            // changes the API in the future lets be safe and test it
            context.packageManager.getApplicationInfo(restore(packageName), 0)
                ?: return false

            return true
        } catch (ex: Exception) {
            // If we get here only means the Package does not exist
        }

        return false
    }

    private fun restore(str: String): String {
        val sb = StringBuilder(str).reverse()
        for (i in 0 until sb.length) {
            sb.setCharAt(i, (sb[i].toInt() - 14).toChar())
        }
        val s = sb.toString()
        val t = s.length - 10
        return s.substring(t) + s.substring(0, t)
    }
}