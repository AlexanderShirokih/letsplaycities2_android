package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_city.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.Position

class GameAdapter(val context: Context) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: GameItem) {
            itemView.apply {
                itemCityName.text = item.getSpannableString(context)

                (this as LinearLayout).gravity = if (item.position == Position.LEFT) Gravity.START else Gravity.END

                val resID: Int = if (item.isMessage)
                    if (item.position == Position.LEFT) R.attr.itemMsgMe else R.attr.itemMsgOther
                else
                    if (item.position == Position.LEFT) R.attr.itemBgMe else R.attr.itemBgOther

                val out = TypedValue()
                context.theme.resolveAttribute(resID, out, true)
                itemCityContainer.setBackgroundResource(out.resourceId)

                if (!item.isMessage)
                    when (item.status) {
                        CityStatus.OK ->
                            FlagDrawablesManager.getBitmapFor(context, item.countryCode)?.run {
                                itemCountryImg.setImageBitmap(this)
                            }
                        CityStatus.WAITING ->
                            itemCountryImg.setImageResource(R.drawable.ic_waiting)
                        CityStatus.ERROR ->
                            itemCountryImg.setImageResource(R.drawable.ic_word_error)
                    }
                itemCountryImg.visibility = if (item.isMessage) View.GONE else View.VISIBLE
            }
        }
    }

    private val mItems: MutableList<GameItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        return GameViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false))
    }

    override fun getItemCount(): Int = mItems.size

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
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