package ru.aleshi.letsplaycities.ui.game

import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.combo_badge.view.*
import kotlinx.android.synthetic.main.fragment_game.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.combos.ComboType

class ComboBadgeView (private val gameFragment: GameFragment) : ComboSystemView {

    private lateinit var quickTv: TextView
    private lateinit var shortTv: TextView
    private lateinit var longTv: TextView
    private lateinit var countryTv: TextView
    private lateinit var diffCountryTv: TextView
    private lateinit var view: View

    private var initialized = false
    private var activeBadges = 0

    override fun init() {
        view =
            gameFragment.layoutInflater.inflate(R.layout.combo_badge, gameFragment.badgeRoot, false)
        gameFragment.badgeRoot.addView(view)
        quickTv = view.iconQuick
        shortTv = view.iconShort
        longTv = view.iconLong
        countryTv = view.iconCountry
        diffCountryTv = view.iconDiffCountry

        initialized = true
    }

    override fun deleteBadge(comboType: ComboType) {
        if (!initialized) return
        if (--activeBadges == 0)
            view.visibility = View.GONE
        getView(comboType).visibility = View.GONE
    }

    override fun addBadge(comboType: ComboType) {
        if (!initialized) return
        if (++activeBadges == 1)
            view.visibility = View.VISIBLE
        getView(comboType).visibility = View.VISIBLE
    }

    override fun updateBadge(comboType: ComboType, multiplier: Float) {
        if (!initialized) return
        getView(comboType).apply {
            text = context.getString(R.string.multiplier, multiplier)
        }
        view.description.text =
            view.context.resources.getStringArray(R.array.field_names)[comboType.ordinal]
    }

    private fun getView(comboType: ComboType) = when (comboType) {
        ComboType.QUICK_TIME -> quickTv
        ComboType.SHORT_WORD -> shortTv
        ComboType.LONG_WORD -> longTv
        ComboType.SAME_COUNTRY -> countryTv
        ComboType.DIFFERENT_COUNTRIES -> diffCountryTv
    }

}