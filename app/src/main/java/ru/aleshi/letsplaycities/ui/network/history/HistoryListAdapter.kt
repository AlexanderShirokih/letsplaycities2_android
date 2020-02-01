package ru.aleshi.letsplaycities.ui.network.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.item_history.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.BasicListAdapter
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.aleshi.letsplaycities.utils.StringUtils
import ru.quandastudio.lpsclient.model.HistoryInfo

class HistoryListAdapter(private val onItemClickListener: OnRemovableItemClickListener<HistoryInfo>) :
    BasicListAdapter<HistoryInfo, HistoryListAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        val holder = HistoryViewHolder(view)
        return holder
    }

    class HistoryViewHolder(view: View) : BasicListAdapter.ViewHolder<HistoryInfo>(view) {
        override fun bind(item: HistoryInfo) {
            itemView.iconFriends.isVisible = item.isFriend
            itemView.item_name.text = item.login
            itemView.tvStartTime.text = StringUtils.formatDate(item.startTime)
            itemView.tvDuration.text = StringUtils.timeFormat(item.duration * 1000L)
            itemView.tvWordsCount.text = StringUtils.formatWordsCount(item.wordsCount)
        }
    }

}