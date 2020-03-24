package ru.aleshi.letsplaycities.ui.citieslist

import androidx.recyclerview.widget.DiffUtil
import ru.aleshi.letsplaycities.base.dictionary.CountryEntity

class CountryEntitiesDiffUtil(
    private val oldList: List<Pair<CountryEntity, Boolean>>,
    private val newList: List<Pair<CountryEntity, Boolean>>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition].first
        val new = newList[newItemPosition].first
        return old.name == new.name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old == new
    }
}