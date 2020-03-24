package ru.aleshi.letsplaycities.ui.citieslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.aleshi.letsplaycities.base.dictionary.CountryEntity
import ru.aleshi.letsplaycities.utils.StringUtils.toTitleCase

/**
 * [RecyclerView.Adapter] for [CountryFilterDialog].
 */
class CountryFilterAdapter(
    private val onItemClicked: (position: Int, isChecked: Boolean) -> Boolean
) : RecyclerView.Adapter<CountryFilterAdapter.ViewHolder>(),
    Observer<List<Pair<CountryEntity, Boolean>>> {

    private var list: List<Pair<CountryEntity, Boolean>>? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Pair<CountryEntity, Boolean>) {
            val textView = itemView.findViewById<CheckedTextView>(android.R.id.text1)
            textView.text = item.first.name.toTitleCase()
            textView.isChecked = item.second

            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    if (this@CountryFilterAdapter.onItemClicked(adapterPosition, !item.second))
                        textView.isChecked = !item.second
                }
            }
        }
    }

    override fun getItemCount(): Int = list?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                android.R.layout.simple_list_item_multiple_choice,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list!![position])

    override fun onChanged(t: List<Pair<CountryEntity, Boolean>>?) {
        val diffUtil = CountryEntitiesDiffUtil(this.list ?: emptyList(), t ?: emptyList())
        this.list = t
        DiffUtil.calculateDiff(diffUtil, false).dispatchUpdatesTo(this)
    }
}