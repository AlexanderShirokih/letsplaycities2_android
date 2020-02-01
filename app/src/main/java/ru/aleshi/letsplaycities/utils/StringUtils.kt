package ru.aleshi.letsplaycities.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object StringUtils {

    fun toTitleCase(input: String): String {
        val sb = StringBuilder()
        for (i in input.indices) {
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

    fun replaceWhitespaces(city: String): String = city.replace(" ", "-")

    fun formatCity(city: String): String {
        val s = city.trim().toLowerCase()
        val replaced = s.replace("ё", "е")
        val sb = StringBuilder()
        var prev: Char = 0.toChar()
        for (element in replaced) {
            val c = element

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

    fun formatDate(time: Long): String {
        val date = Date(time)
        val todayCalendar = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply { this.time = date }

        val day = calendar.get(Calendar.DAY_OF_YEAR)
        val tDay = todayCalendar.get(Calendar.DAY_OF_YEAR)

        return SimpleDateFormat(
            when {
                day == tDay -> "'Сегодня, 'HH:mm"
                tDay == 1 && (day == 365 || day == 366) || day + 1 == tDay -> "'Вчера, 'HH:mm"
                abs(day - tDay) > 5 -> "dd.MM.yy"
                else -> "dd.MM.yy HH:mm"
            }, Locale.getDefault()
        ).format(date)
    }

    fun formatWordsCount(words: Int): String {
        if (words in 5..20) return "$words слов"

        return when (words.toString().last()) {
            '1' -> "$words слово"
            in '2'..'4' -> "$words слова"
            else -> "$words слов"
        }
    }

    fun findLastSuitableChar(city: String): Char? {
        return city.reversed().toCharArray()
            .find { it != 'ь' && it != 'ъ' && it != 'ы' && it != 'ё' }
    }
}