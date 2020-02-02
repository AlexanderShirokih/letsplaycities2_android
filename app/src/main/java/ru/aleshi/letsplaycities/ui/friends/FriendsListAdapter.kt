package ru.aleshi.letsplaycities.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_friends.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.BasicListAdapter
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.quandastudio.lpsclient.model.FriendInfo

class FriendsListAdapter(
    private val mPicasso: Picasso,
    private val mFriendsItemListener: OnRemovableItemClickListener<FriendInfo>
) :
    BasicListAdapter<FriendInfo, FriendsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friends, parent, false)
        val holder = FriendsViewHolder(mPicasso, view)

        view.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                mFriendsItemListener.onItemClicked(getItem(holder.adapterPosition))
            }
        }

        view.btn_remove.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                mFriendsItemListener.onRemoveItemClicked(getItem(pos), holder.adapterPosition)
            }
        }

        return holder
    }

}