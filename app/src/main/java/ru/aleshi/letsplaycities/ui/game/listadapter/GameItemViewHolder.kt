package ru.aleshi.letsplaycities.ui.game.listadapter

import androidx.recyclerview.widget.RecyclerView
import ru.aleshi.letsplaycities.ui.game.GameEntityWrapper

class GameItemViewHolder(view: GameItemView) : RecyclerView.ViewHolder(view) {
    fun bind(entityWrapper: GameEntityWrapper) = (itemView as GameItemView).bind(entityWrapper)
}