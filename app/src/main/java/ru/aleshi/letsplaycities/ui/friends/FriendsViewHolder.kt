package ru.aleshi.letsplaycities.ui.friends

import android.view.View
import kotlinx.android.synthetic.main.item_friends.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.BasicListAdapter
import ru.quandastudio.lpsclient.model.FriendInfo

class FriendsViewHolder(view: View) : BasicListAdapter.ViewHolder<FriendInfo>(view) {

    override fun bind(item: FriendInfo) {
        var value = item.login
        if (!item.accepted) {
            value += itemView.context.getString(R.string.request)
        }
        itemView.item_name.text = value
    }
}
