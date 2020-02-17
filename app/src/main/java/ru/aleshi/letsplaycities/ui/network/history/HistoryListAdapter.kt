package ru.aleshi.letsplaycities.ui.network.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_history.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.BasicListAdapter
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.aleshi.letsplaycities.utils.StringUtils
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.HistoryInfo

class HistoryListAdapter(
    private val picasso: Picasso,
    private val onItemClickListener: OnRemovableItemClickListener<HistoryInfo>
) :
    BasicListAdapter<HistoryInfo, HistoryListAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(picasso, view)
    }

    class HistoryViewHolder(private val picasso: Picasso, view: View) :
        BasicListAdapter.ViewHolder<HistoryInfo>(view) {
        override fun bind(item: HistoryInfo) {
            itemView.iconFriends.isVisible = item.isFriend
            itemView.item_name.text = item.login
            itemView.tvStartTime.text = StringUtils.formatDate(item.creationDate)
            itemView.tvDuration.text = StringUtils.timeFormat(item.duration * 1000L)
            itemView.tvWordsCount.text = StringUtils.formatWordsCount(item.wordsCount)

            picasso.load(Utils.getPictureUrl(item.userId, item.pictureHash))
                .placeholder(R.drawable.ic_player)
                .error(R.drawable.ic_player)
                .into(itemView.iv_picture)
        }

    }

}