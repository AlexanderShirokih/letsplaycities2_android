package ru.aleshi.letsplaycities.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_settings.view.*
import ru.aleshi.letsplaycities.R

class SettingsListAdapter(private val list: List<SettingsItem>, private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<SettingsListAdapter.SettingsItemViewHolder>() {
    interface OnItemClickListener {
        fun onItemClicked(position: Int, value: SettingsItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false)
        return SettingsItemViewHolder(view, clickListener)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: SettingsItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun updateItem(position: Int, value: Int) {
        list[position].currentValuePosition = value
        notifyItemChanged(position)
    }

    class SettingsItemViewHolder(private val view: View, private val clickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(view) {
        fun bind(settingsItem: SettingsItem) {
            view.item_sett_name.text = settingsItem.name
            view.item_sett_val.text = settingsItem.currentValue

            if (!settingsItem.canBeEnabled()) {
                view.switch1.visibility = View.GONE
                view.switch1.setOnCheckedChangeListener(null)
                view.setOnClickListener {
                    clickListener.onItemClicked(adapterPosition, settingsItem)
                }
            } else {
                val switchListener: CompoundButton.OnCheckedChangeListener =
                    CompoundButton.OnCheckedChangeListener { switch, _ ->
                        onClick(
                            switch,
                            settingsItem
                        ) { switch.isChecked = it }
                    }

                view.setOnClickListener(null)
                view.switch1.visibility = View.VISIBLE
                view.switch1.isChecked = settingsItem.isEnabled()
                view.switch1.setOnCheckedChangeListener(switchListener)
                view.setOnClickListener {
                    onClick(view.switch1, settingsItem) {
                        view.switch1.apply {
                            setOnCheckedChangeListener(null)
                            isChecked = it
                            setOnCheckedChangeListener(switchListener)
                        }
                    }
                }
            }
        }

        private fun onClick(
            switch: CompoundButton,
            settingsItem: SettingsItem,
            toggleListener: (isChecked: Boolean) -> Unit
        ) {
            if (!settingsItem.hasAdvancedVariants())
                settingsItem.next()
            clickListener.onItemClicked(adapterPosition, settingsItem)
            view.item_sett_val.text = settingsItem.currentValue
            toggleListener(settingsItem.isEnabled())
            switch.isChecked = settingsItem.isEnabled()
        }
    }
}