package ru.aleshi.letsplaycities.ui.friends

import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_friends.view.*
import kotlinx.android.synthetic.main.item_friends.view.item_name
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.BasicListAdapter
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.FriendInfo

class FriendsViewHolder(private val picasso: Picasso, view: View) :
    BasicListAdapter.ViewHolder<FriendInfo>(view) {

    override fun bind(item: FriendInfo) {
        var value = item.login
        if (!item.accepted) {
            value += itemView.context.getString(R.string.request)
        }
        itemView.item_name.text = value
        picasso.load(Utils.getPictureUri(item.userId, item.pictureHash))
            .placeholder(R.drawable.ic_player)
            .error(R.drawable.ic_player)
            .into(itemView.iv_friends_item)
    }
}
