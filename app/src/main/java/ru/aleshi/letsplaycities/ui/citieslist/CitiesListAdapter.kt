package ru.aleshi.letsplaycities.ui.citieslist

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_city.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.citieslist.CityItem
import ru.aleshi.letsplaycities.ui.game.FlagDrawablesManager

/**
 * Adapter for [CitiesListFragment] which can bind cities items.
 */
class CitiesListAdapter : RecyclerView.Adapter<CitiesListAdapter.CityViewHolder>() {

    private var items: List<CityItem>? = null

    /**
     * ViewHolder for cities list adapter
     */
    class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(cityItem: CityItem) {
            itemView.cityName.text = cityItem.city
            itemView.countryHint.text =
                if (cityItem.country.isEmpty()) itemView.context.getString(R.string.unknown_country) else cityItem.country

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

    suspend fun onUpdatesDelivered(update: Pair<List<CityItem>, Boolean>) {
        val (newList, isAllPresent) = update

        if (isAllPresent || newList.isEmpty()) {
            this.items = newList
            notifyDataSetChanged()
            return
        }

        val diffResult = withContext(Dispatchers.Default) {
            DiffUtil.calculateDiff(
                CitiesListDiffUtil(this@CitiesListAdapter.items ?: emptyList(), newList)
            )
        }
        this.items = newList
        diffResult.dispatchUpdatesTo(this)
    }

}