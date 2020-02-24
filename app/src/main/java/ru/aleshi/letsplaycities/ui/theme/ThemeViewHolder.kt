package ru.aleshi.letsplaycities.ui.theme

import android.view.View
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_theme.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.GameEntity
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.ui.game.CityStatus
import ru.aleshi.letsplaycities.ui.game.GameEntityWrapper

class ThemeViewHolder(private val listener: ThemeItemClickListener, private val view: View) :
    RecyclerView.ViewHolder(view) {

    fun bind(namedTheme: ThemeListAdapter.NamedTheme) {
        bindTheme(namedTheme.theme.themeId)

        view.textView.text = namedTheme.name
        view.sampleGameView1.bind(createGameItem(view, 0), namedTheme.theme)
        view.sampleGameView2.bind(createGameItem(view, 1), namedTheme.theme)
        view.sampleGameView3.bind(createGameItem(view, 2), namedTheme.theme)
        view.btn_unlock.visibility =
            if (namedTheme.theme.isFreeOrAvailable()) View.GONE else View.VISIBLE
        view.btn_unlock.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener.onUnlock(namedTheme)
        }
        view.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener.onSelectTheme(namedTheme)
        }

    }

    private fun bindTheme(themeId: Int) {
        val typedValue = view.context.obtainStyledAttributes(
            themeId, intArrayOf(
                R.attr.colorPrimary,
                R.attr.colorOnPrimary,
                android.R.attr.windowBackground
            )
        )

        view.topView.setBackgroundColor(typedValue.getColorOrThrow(0))
        view.textView.setTextColor(typedValue.getColorOrThrow(1))
        view.btn_unlock.setColorFilter(typedValue.getColorOrThrow(1))
        view.itemBackground.setBackgroundResource(typedValue.getResourceIdOrThrow(2))

        typedValue.recycle()
    }

    private fun createGameItem(view: View, index: Int): GameEntityWrapper {
        val sampleCities = view.resources.getStringArray(R.array.sampleCities)
        val sampleCitiesCountryCode = view.resources.getIntArray(R.array.sampleCitiesCountryCode)
        return GameEntityWrapper(
            GameEntity.CityInfo(
                city = sampleCities[index],
                countryCode = sampleCitiesCountryCode[index].toShort(),
                position = if (index % 2 == 0) Position.RIGHT else Position.LEFT,
                status = CityStatus.OK
            )
        )
    }
}