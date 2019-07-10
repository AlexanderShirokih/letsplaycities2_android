package ru.aleshi.letsplaycities.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_friends.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo

class FriendsListAdapter(private val mFriendsItemListener: FriendsItemListener) :
    RecyclerView.Adapter<FriendsViewHolder>() {

    private var list: MutableList<FriendsInfo> = mutableListOf()

    fun updateItems(list: MutableList<FriendsInfo>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun removeItem(item: FriendsInfo) {
        val index = list.indexOf(item)
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friends, parent, false)
        val holder = FriendsViewHolder(view)

        view.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                mFriendsItemListener.onFriendsItemClicked(list[holder.adapterPosition])
            }
        }

        view.btn_remove.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                mFriendsItemListener.onRemoveFriendsItem(list[pos])
            }
        }

        return holder
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        holder.bind(list[position])
    }

}