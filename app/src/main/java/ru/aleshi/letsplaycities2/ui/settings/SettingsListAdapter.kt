package ru.aleshi.letsplaycities2.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_settings.view.*
import ru.aleshi.letsplaycities2.R

class SettingsListAdapter(private val list: List<SettingsItem>, private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<SettingsListAdapter.SettingsItemViewHolder>() {
    interface OnItemClickListener {
        fun onItemClicked(position: Int, value: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false)
        val holder = SettingsItemViewHolder(view)
        view.setOnClickListener {
            if(holder.adapterPosition != RecyclerView.NO_POSITION) {
                val item = list[holder.adapterPosition]
                if (item.isEmpty())
                    clickListener.onItemClicked(holder.adapterPosition, -1)
                else {
                    item.next()
                    clickListener.onItemClicked(holder.adapterPosition, item.currentValuePosition)
                    notifyItemChanged(holder.adapterPosition)
                }
            }
        }
        return holder
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: SettingsItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class SettingsItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(settingsItem: SettingsItem) {
            view.item_sett_name.text = settingsItem.name
            view.item_sett_val.text = if (settingsItem.isEmpty()) "" else settingsItem.currentValue
        }
    }
}