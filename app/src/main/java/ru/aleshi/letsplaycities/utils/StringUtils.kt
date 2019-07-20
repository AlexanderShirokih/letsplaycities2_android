package ru.aleshi.letsplaycities.utils

object StringUtils {

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


    fun formatCity(city: String): String {
        val s = city.trim().toLowerCase()
        val replaced = s.replace(" ", "-").replace("ё", "е")
        val sb = StringBuilder()
        var prev: Char = 0.toChar()
        for (i in 0 until replaced.length) {
            val c = replaced[i]

            if (!(c == '-' && prev == '-')) {
                sb.append(c)
            }
            prev = c
        }
        return sb.toString()
    }

    fun timeFormat(time: Long): String {
        var t = time.toInt()
        var ret = ""
        val min = t / 60000
        t -= min * 60000
        t /= 1000
        if (min > 0) {
            ret = min.toString() + "мин "
        }
        ret += t.toString() + "сек"
        return ret
    }

    fun findLastSuitableChar(city: String): Char? {
        return city.reversed().toCharArray()
            .find { it != 'ь' && it != 'ъ' && it != 'ы' && it != 'ё' }
    }
}