package ru.aleshi.letsplaycities.ui.friends

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_friends.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo

class FriendsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(info: FriendsInfo) {
        var value = info.name
        if (!info.isAccepted) {
            value += itemView.context.getString(R.string.request)
        }
        itemView.item_name.text = value
    }
}
