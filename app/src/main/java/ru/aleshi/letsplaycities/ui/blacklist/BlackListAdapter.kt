package ru.aleshi.letsplaycities.ui.blacklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_blacklist.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.BasicListAdapter
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.BlackListItem

class BlackListAdapter(
    private val mPicasso: Picasso,
    private val onItemClickListener: OnRemovableItemClickListener<BlackListItem>
) :
    BasicListAdapter<BlackListItem, BlackListAdapter.BlackListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlackListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_blacklist, parent, false)
        val holder = BlackListViewHolder(mPicasso, view)
        view.btn_remove.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION)
                onItemClickListener.onRemoveItemClicked(
                    getItem(holder.adapterPosition),
                    holder.adapterPosition
                )
        }
        return holder
    }

    class BlackListViewHolder(private val picasso: Picasso, private val view: View) :
        BasicListAdapter.ViewHolder<BlackListItem>(view) {

        override fun bind(item: BlackListItem) {
            view.item_name.text = item.login
            picasso.load(Utils.getPictureUri(item.userId, item.pictureHash))
                .placeholder(R.drawable.ic_player_big)
                .error(R.drawable.ic_player_big)
                .into(itemView.iv_friends_item)
        }

    }

}