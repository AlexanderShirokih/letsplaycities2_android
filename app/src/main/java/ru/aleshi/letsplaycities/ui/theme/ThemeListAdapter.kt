package ru.aleshi.letsplaycities.ui.theme

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.Theme

class ThemeListAdapter(private val list: Array<NamedTheme>, private val listener: ThemeItemClickListener) :
    RecyclerView.Adapter<ThemeViewHolder>() {

    class NamedTheme(val name: String, val theme: Theme)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_theme, parent, false)
        return ThemeViewHolder(listener, view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun notifyItemChanged(theme: Theme) {
        notifyItemChanged(list.indexOfFirst { it.theme == theme })
    }

}
