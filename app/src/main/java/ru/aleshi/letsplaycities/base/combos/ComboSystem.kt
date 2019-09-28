package ru.aleshi.letsplaycities.base.combos

import java.util.*
import kotlin.math.max
import kotlin.math.min

class ComboSystem(private val view: ComboSystemView, private val canUseQuickTime: Boolean) {

    private val maxSingleScore = 3f
    private val activeCombos = mutableMapOf<ComboType, Int>()
    private val infoList: LinkedList<CityComboInfo> = LinkedList()

    val multiplier: Float
        get() = max(activeCombos.values.map { getScore(it) }.sum(), 1f)

    val activeCombosList: Map<ComboType, Int>
        get() = activeCombos

    fun addCity(cityComboInfo: CityComboInfo) {
        infoList.add(cityComboInfo)
        updateCombos()
    }

    private fun updateCombos() {
        val usedCountries = mutableSetOf<Short>()
        val lastCountryCode = infoList.lastOrNull()?.countryCode ?: 0

        updateCombo(ComboType.QUICK_TIME) { canUseQuickTime && it.isQuick }
        updateCombo(ComboType.SHORT_WORD) { it.isShort }
        updateCombo(ComboType.LONG_WORD) { it.isLong }
        updateCombo(ComboType.SAME_COUNTRY) { it.countryCode > 0 && it.countryCode == lastCountryCode }
        updateCombo(ComboType.DIFFERENT_COUNTRIES) {
            val res = !usedCountries.contains(it.countryCode)
            usedCountries.add(it.countryCode)
            res
        }
        usedCountries.clear()
    }

    private fun updateCombo(type: ComboType, predicate: (info: CityComboInfo) -> Boolean) {
        val quickCombo = getCombo(type.minSize, predicate)

        if (quickCombo > 0) {
            if (!activeCombos.containsKey(type))
                view.addBadge(type)
            view.updateBadge(type, getScore(quickCombo))
            activeCombos[type] = quickCombo
        } else {
            activeCombos.remove(type)?.let {
                view.deleteBadge(type)
            }
        }
    }

    private fun getCombo(minComboSize: Int, predicate: (info: CityComboInfo) -> Boolean): Int {
        return max(infoList.takeLastWhile(predicate).size - minComboSize + 1, 0)
    }

    private fun getScore(s: Int) = min((s + 1) * 0.5f + 0.5f, maxSingleScore)

    fun clear() {
        infoList.clear()
        updateCombos()
    }
}