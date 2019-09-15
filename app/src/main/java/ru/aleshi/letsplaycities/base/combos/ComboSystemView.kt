package ru.aleshi.letsplaycities.base.combos

interface ComboSystemView {

    fun deleteBadge(comboType: ComboType) {}

    fun addBadge(comboType: ComboType) {}

    fun updateBadge(comboType: ComboType, multiplier: Float) {}
}