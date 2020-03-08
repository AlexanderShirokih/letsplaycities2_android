package ru.aleshi.letsplaycities.ui.game.listadapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.aleshi.letsplaycities.ui.game.GameEntityWrapper

class GameAdapter(val context: Context) : RecyclerView.Adapter<GameItemViewHolder>() {

    private var entityWrappers: MutableList<GameEntityWrapper> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        GameItemViewHolder(
            GameItemView(
                context
            )
        )

    override fun getItemCount(): Int = entityWrappers.size

    override fun onBindViewHolder(holder: GameItemViewHolder, position: Int) {
        holder.bind(entityWrappers[position])
    }

    /**
     * Call to notify about items update
     */
    fun updateEntities(citiesList: List<GameEntityWrapper>) {
        val diffUtil = GameItemDiffUtil(entityWrappers, citiesList)
        DiffUtil.calculateDiff(diffUtil, false).dispatchUpdatesTo(this)
        entityWrappers.clear()
        entityWrappers.addAll(citiesList)
    }

}