package ru.aleshi.letsplaycities.ui.friends

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_friends.view.*
import ru.aleshi.letsplaycities.R
import ru.quandastudio.lpsclient.model.FriendInfo

class FriendsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(info: FriendInfo) {
        var value = info.login
        if (!info.accepted) {
            value += itemView.context.getString(R.string.request)
        }
        itemView.item_name.text = value
    }
}
