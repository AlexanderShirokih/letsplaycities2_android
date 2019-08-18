package ru.aleshi.letsplaycities.base.combos

import java.util.*
import kotlin.math.max

class ComboSystem(private val view: ComboSystemView) {

    private val minComboSize = 3 - 1
    private val activeCombos = mutableMapOf<ComboType, Int>()
    private val infoList: LinkedList<CityComboInfo> = LinkedList()


    val multiplier: Float
        get() = max(activeCombos.values.map { getScore(it) }.sum(), 1f)

    fun addCity(cityComboInfo: CityComboInfo) {
        infoList.add(cityComboInfo)
        updateCombos()
    }

    private fun updateCombos() {
        updateCombo(ComboType.QUICK_TIME) { it.isQuick }
        updateCombo(ComboType.SHORT_WORD) { it.isShort }
        updateCombo(ComboType.LONG_WORD) { it.isLong }
    }

    private fun updateCombo(type: ComboType, predicate: (info: CityComboInfo) -> Boolean) {
        val quickCombo = getCombo(predicate)

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

    private fun getCombo(predicate: (info: CityComboInfo) -> Boolean): Int {
        return max(infoList.takeLastWhile(predicate).size - minComboSize, 0)
    }

    private fun getScore(s: Int) = (s + 1) * 0.5f + 0.5f

    fun clear() {
        infoList.clear()
        updateCombos()
    }
}