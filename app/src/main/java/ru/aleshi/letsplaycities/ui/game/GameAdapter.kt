package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.aleshi.letsplaycities.base.game.Position

class GameAdapter(val context: Context) : RecyclerView.Adapter<GameItemViewHolder>() {

    private val mItems: MutableList<GameItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GameItemViewHolder(GameItemView(context))

    override fun getItemCount(): Int = mItems.size

    override fun onBindViewHolder(holder: GameItemViewHolder, position: Int) {
        holder.bind(mItems[position])
    }

    fun addCity(city: String, countryCode: Short, position: Position) {
        mItems.add(GameItem(city, position, CityStatus.WAITING, countryCode = countryCode))
        notifyItemInserted(mItems.size - 1)
    }

    fun updateCity(city: String, hasErrors: Boolean) {
        val index = mItems.indexOfFirst { it.content == city }
        if (index > -1) {
            mItems[index].status = if (hasErrors) CityStatus.ERROR else CityStatus.OK
            notifyItemChanged(index)
        }
    }

    fun addMessage(message: String, position: Position) {
        mItems.add(GameItem(message, position, CityStatus.OK, true))
    }

    fun clear() {
        mItems.clear()
        notifyDataSetChanged()
    }

}