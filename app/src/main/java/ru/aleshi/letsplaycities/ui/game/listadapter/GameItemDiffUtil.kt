package ru.aleshi.letsplaycities.ui.game.listadapter

import androidx.recyclerview.widget.DiffUtil
import ru.aleshi.letsplaycities.ui.game.GameEntityWrapper

/**
 * Finds differences in game items.
 */

class GameItemDiffUtil(
    private val oldList: List<GameEntityWrapper>,
    private val newList: List<GameEntityWrapper>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition].gameEntity
        val new = newList[newItemPosition].gameEntity
        return old.areTheSameWith(new)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition].gameEntity
        val new = newList[newItemPosition].gameEntity
        return old == new
    }
}