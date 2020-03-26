package ru.aleshi.letsplaycities.ui.citieslist

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_city.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.game.FlagDrawablesManager

/**
 * Adapter for [CitiesListFragment] which can bind cities items.
 */
class CitiesListAdapter : RecyclerView.Adapter<CitiesListAdapter.CityViewHolder>(),
    Observer<List<CityItem>?> {

    private var items: List<CityItem>? = null

    /**
     * ViewHolder for cities list adapter
     */
    class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(cityItem: CityItem) {
            itemView.cityName.text = cityItem.city
            itemView.countryHint.text = cityItem.country

            FlagDrawablesManager.getBitmapFor(
                itemView.context,
                cityItem.countryCode
            )?.run {
                itemView.cityName.setCompoundDrawablesWithIntrinsicBounds(
                    BitmapDrawable(
                        itemView.context.resources,
                        this
                    ),
                    null, null, null
                )
            }
        }
    }

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder =
        CityViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_city, parent, false)
        )

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) =
        holder.bind(items!![position])

    override fun onChanged(t: List<CityItem>?) {
        val diffUtil = CitiesListDiffUtil(this.items ?: emptyList(), t ?: emptyList())
        this.items = t
        DiffUtil.calculateDiff(diffUtil, false).dispatchUpdatesTo(this)
    }

}