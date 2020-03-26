package ru.aleshi.letsplaycities.base.dictionary

class CityProperties(var diff: Byte, var countryCode: Short) {
    fun resetUsageFlag() {
        if (diff < 0) diff = (-diff).toByte()
    }

    fun isNotUsed() = diff > 0

    fun markUsed() {
        if (diff > 0) diff = (-diff).toByte()
    }
}