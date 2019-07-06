package ru.aleshi.letsplaycities2.utils

import android.content.Context

object Utils {
    fun checkRateDialog(context: Context) {
        //TODO
    }

    fun firstToUpper(input: String): String {
        val sb = StringBuilder()
        for (i in 0 until input.length) {
            var c = input[i]
            if (i == 0 || input[i - 1] == '-' || input[i - 1] == ' ')
                c = Character.toUpperCase(c)
            sb.append(c)
        }
        return sb.toString()
    }

    fun formatName(name: String): String {
        val ind = name.indexOf(" ")
        return if (ind > 0) "${name.substring(0, ind)}а${name.substring(ind)}" else "${name}а"
    }
}