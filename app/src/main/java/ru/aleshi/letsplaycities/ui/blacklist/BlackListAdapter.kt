package ru.aleshi.letsplaycities.ui.blacklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_blacklist.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.BasicListAdapter
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.quandastudio.lpsclient.model.BlackListItem

class BlackListAdapter(private val onItemClickListener: OnRemovableItemClickListener<BlackListItem>) :
    BasicListAdapter<BlackListItem, BlackListAdapter.BlackListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlackListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_blacklist, parent, false)
        val holder = BlackListViewHolder(view)
        view.btn_remove.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION)
                onItemClickListener.onRemoveItemClicked(
                    getItem(holder.adapterPosition),
                    holder.adapterPosition
                )
        }
        return holder
    }

    class BlackListViewHolder(private val view: View) :
        BasicListAdapter.ViewHolder<BlackListItem>(view) {

        override fun bind(item: BlackListItem) {
            view.item_name.text = item.login
        }

    }

}