package ru.aleshi.letsplaycities.ui.game

import androidx.recyclerview.widget.RecyclerView

class GameItemViewHolder(view: GameItemView) : RecyclerView.ViewHolder(view) {
    fun bind(item: GameItem) = (itemView as GameItemView).bind(item)
}