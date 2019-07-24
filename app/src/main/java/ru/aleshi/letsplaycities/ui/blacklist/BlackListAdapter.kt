package ru.aleshi.letsplaycities.ui.blacklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_blacklist.view.*
import ru.aleshi.letsplaycities.R

class BlackListAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<BlackListAdapter.BlackListViewHolder>() {

    private var list: MutableList<BlackListItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlackListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blacklist, parent, false)
        val holder = BlackListViewHolder(view)
        view.btn_remove.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION)
                onItemClickListener.onRemove(list[holder.adapterPosition], holder.adapterPosition)
        }
        return holder
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BlackListViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun remove(pos: Int) {
        list.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun updateItems(newList: MutableList<BlackListItem>) {
        list = newList
        notifyDataSetChanged()
    }

    class BlackListViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(s: BlackListItem) {
            view.item_name.text = s.userName
        }

    }

}