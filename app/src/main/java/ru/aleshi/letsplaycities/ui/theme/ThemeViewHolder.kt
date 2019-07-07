package ru.aleshi.letsplaycities.ui.theme

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_theme.view.*

class ThemeViewHolder(private val listener: ThemeItemClickListener, private val view: View) :
    RecyclerView.ViewHolder(view) {

    fun bind(namedTheme: ThemeListAdapter.NamedTheme) {
        view.textView.text = namedTheme.name
        view.btn_unlock.visibility = if (namedTheme.theme.isFreeOrAvailable()) View.GONE else View.VISIBLE
        view.btn_unlock.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener.onUnlock(namedTheme)
        }
        view.btn_show_preview.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener.onShowPreview(namedTheme, adapterPosition)
        }
        view.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener.onSelectTheme(namedTheme)
        }
    }
}