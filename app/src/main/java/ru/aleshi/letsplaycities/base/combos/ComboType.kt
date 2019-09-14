package ru.aleshi.letsplaycities.base.combos

enum class ComboType constructor(val minSize: Int) {
    QUICK_TIME(3),
    SHORT_WORD(3),
    LONG_WORD(3),
    SAME_COUNTRY(7)
}