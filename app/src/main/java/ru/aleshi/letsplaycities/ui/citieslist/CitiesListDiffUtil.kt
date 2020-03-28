package ru.aleshi.letsplaycities.ui.citieslist

import androidx.recyclerview.widget.DiffUtil
import ru.aleshi.letsplaycities.base.citieslist.CityItem

class CitiesListDiffUtil(
    private val old: List<CityItem>,
    private val new: List<CityItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]

        return oldItem.city == newItem.city
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]

        return oldItem == newItem
    }
}