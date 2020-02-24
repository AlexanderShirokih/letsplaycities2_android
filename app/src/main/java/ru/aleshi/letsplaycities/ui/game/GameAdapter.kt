package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.aleshi.letsplaycities.base.game.GameEntity
import ru.aleshi.letsplaycities.base.game.Position

class GameAdapter(val context: Context) : RecyclerView.Adapter<GameItemViewHolder>() {

    private val mEntityWrappers: MutableList<GameEntityWrapper> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        GameItemViewHolder(GameItemView(context))

    override fun getItemCount(): Int = mEntityWrappers.size

    override fun onBindViewHolder(holder: GameItemViewHolder, position: Int) {
        holder.bind(mEntityWrappers[position])
    }

    fun addCity(city: String, countryCode: Short, position: Position) {
        mEntityWrappers.add(
            GameEntityWrapper(
                GameEntity.CityInfo(
                    city = city,
                    position = position,
                    status = CityStatus.WAITING,
                    countryCode = countryCode
                )
            )
        )
        notifyItemInserted(mEntityWrappers.size - 1)
    }

    fun updateCity(cityInfo: GameEntity.CityInfo) {
        val index =
            mEntityWrappers.indexOfFirst { it.gameEntity is GameEntity.CityInfo && it.gameEntity.city == cityInfo.city }
        if (index > -1) {
            mEntityWrappers[index] = GameEntityWrapper(cityInfo)
            notifyItemChanged(index)
        }
    }

    fun addMessage(message: String, position: Position) {
        mEntityWrappers.add(
            GameEntityWrapper(
                GameEntity.MessageInfo(
                    message = message,
                    position = position
                )
            )
        )
    }

    fun clear() {
        mEntityWrappers.clear()
        notifyDataSetChanged()
    }

}