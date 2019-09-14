package ru.aleshi.letsplaycities.base.combos

data class CityComboInfo(
    val isQuick: Boolean,
    val isShort: Boolean,
    val isLong: Boolean,
    val countryCode: Short
) {

    companion object Creator {
        private const val QUICK_MOVE_TIME = 5000
        private const val SHORT_WORD_SIZE = 4
        private const val LONG_WORD_SIZE = 8

        fun create(deltaTimeInMs: Long, word: String, countryCode: Short) = CityComboInfo(
            deltaTimeInMs <= QUICK_MOVE_TIME,
            word.length <= SHORT_WORD_SIZE,
            word.length >= LONG_WORD_SIZE,
            countryCode
        )
    }

}