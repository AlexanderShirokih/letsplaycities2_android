package ru.aleshi.letsplaycities.ui.game

import android.view.View
import kotlinx.android.synthetic.main.combo_badge.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.combos.ComboType

class ComboBadgeView(private val view: View) : ComboSystemView {

    private val quickTv = view.iconQuick
    private val shortTv = view.iconShort
    private val longTv = view.iconLong
    private val countryTv = view.iconCountry

    private var activeBadges = 0

    override fun deleteBadge(comboType: ComboType) {
        if (--activeBadges == 0)
            view.visibility = View.GONE
        getView(comboType).visibility = View.GONE
    }

    override fun addBadge(comboType: ComboType) {
        if (++activeBadges == 1)
            view.visibility = View.VISIBLE
        getView(comboType).visibility = View.VISIBLE
    }

    override fun updateBadge(comboType: ComboType, multiplier: Float) {
        getView(comboType).apply {
            text = context.getString(R.string.multiplier, multiplier)
        }
        view.description.text = view.context.resources.getStringArray(R.array.field_names)[comboType.ordinal]
    }

    private fun getView(comboType: ComboType) = when (comboType) {
        ComboType.QUICK_TIME -> quickTv
        ComboType.SHORT_WORD -> shortTv
        ComboType.LONG_WORD -> longTv
        ComboType.SAME_COUNTRY -> countryTv
    }

}