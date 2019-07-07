package ru.aleshi.letsplaycities.base.scoring

import ru.aleshi.letsplaycities.base.scoring.ScoreManager.Companion.F_TIME
import java.util.*

class ScoringField(val name: String, var value: Any? = null) {

    fun increase() {
        if (value is Int)
            value = value as Int + 1
    }

    fun max(m: Int) {
        if (value is Int)
            value = Math.max(value as Int, m)
    }

    fun add(a: Int) {
        if (value is Int)
            value = (value as Int) + a
    }

    fun value(): String {
        return if (name == F_TIME) sec2time((value as Int?)!!) else value.toString()
    }

    fun set(value: Any) {
        this.value = value
    }

    fun hasValue(): Boolean {
        return value != null
    }

    private fun sec2time(t: Int): String {
        var s = t
        var h = 0
        var m = 0
        if (s > 3600) {
            h = s / 3600
            s -= 3600 * h
        }
        if (s > 60) {
            m = s / 60
            s -= 60 * m
        }

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }
}