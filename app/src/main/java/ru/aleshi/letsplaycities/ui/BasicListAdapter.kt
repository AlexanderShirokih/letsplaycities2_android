package ru.aleshi.letsplaycities.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BasicListAdapter<D, VH : BasicListAdapter.ViewHolder<D>> :
    RecyclerView.Adapter<VH>() {

    abstract class ViewHolder<D>(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: D)
    }

    private var list: MutableList<D> = mutableListOf()


    fun updateItems(newList: List<D>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


    fun removeItem(item: D) {
        val index = list.indexOf(item)
        if (index != -1)
            removeAt(index)
    }

    fun removeAt(pos: Int) {
        list.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun getItem(pos: Int): D = list[pos]

    override fun getItemCount() = list.size


    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position])
    }
}