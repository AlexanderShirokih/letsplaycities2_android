package ru.aleshi.letsplaycities.ui.theme

import android.view.View
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_theme.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.game.GameItem
import kotlin.random.Random

class ThemeViewHolder(private val listener: ThemeItemClickListener, private val view: View) :
    RecyclerView.ViewHolder(view) {

    companion object SampleGenerator {
        @JvmStatic
        var counter: Int = 0
        var index: Int = 0

        @JvmStatic
        private fun createGameItem(view: View): GameItem {
            val sampleCities = view.resources.getStringArray(R.array.sampleCities)
            val sampleCitiesCountryCode = view.resources.getIntArray(R.array.sampleCitiesCountryCode)
            if (counter == 0)
                index = Random.Default.nextInt(sampleCities.size)
            if (counter++ == sampleCities.size + 2) counter = 0
            return GameItem(sampleCities[index], sampleCitiesCountryCode[index].toShort())
        }
    }

    fun bind(namedTheme: ThemeListAdapter.NamedTheme) {
        bindTheme(namedTheme.theme.themeId)

        view.textView.text = namedTheme.name
        view.sampleGameView.bind(createGameItem(view), namedTheme.theme)
        view.btn_unlock.visibility = if (namedTheme.theme.isFreeOrAvailable()) View.GONE else View.VISIBLE
        view.btn_unlock.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener.onUnlock(namedTheme)
        }
        view.btn_show_preview.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener.onShowPreview(namedTheme, adapterPosition)
        }
        view.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener.onSelectTheme(namedTheme)
        }

    }

    private fun bindTheme(themeId: Int) {
        val typedValue = view.context.obtainStyledAttributes(
            themeId, intArrayOf(
                R.attr.colorAccent,
                R.attr.colorOnPrimary,
                android.R.attr.windowBackground
            )
        )

        view.topView.setBackgroundColor(typedValue.getColorOrThrow(0))
        view.textView.setTextColor(typedValue.getColorOrThrow(1))
        view.btn_unlock.setColorFilter(typedValue.getColorOrThrow(1))
        view.btn_show_preview.setColorFilter(typedValue.getColorOrThrow(1))
        view.itemBackground.setBackgroundResource(typedValue.getResourceIdOrThrow(2))

        typedValue.recycle()
    }
}