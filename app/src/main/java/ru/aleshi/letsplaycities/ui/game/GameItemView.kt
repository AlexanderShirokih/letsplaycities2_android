package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.setMargins
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.Theme
import ru.aleshi.letsplaycities.base.ThemeManager
import ru.aleshi.letsplaycities.base.game.Position

class GameItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    private val itemContainer: LinearLayout
    private val wordIcon = ImageView(context, attrs, defStyleAttr)
    private val city = AppCompatTextView(context, attrs, defStyleAttr).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.item_text_size))
    }

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = HORIZONTAL

        itemContainer = LinearLayout(context, attrs, defStyleAttr).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER

            addView(wordIcon, MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(convertToPx(6f))
            })
            addView(city, MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = convertToPx(2f)
            })
        }

        addView(itemContainer, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            setMargins(convertToPx(4f))
        })
    }

    fun bind(item: GameItem) = bind(item, ThemeManager.getCurrentTheme(context))

    fun bind(item: GameItem, theme: Theme) {
        val typedValue = context.obtainStyledAttributes(
            theme.themeId, intArrayOf(
                R.attr.fgSpanColor,
                if (item.isMessage)
                    if (item.position == Position.LEFT) R.attr.itemMsgMe else R.attr.itemMsgOther
                else
                    if (item.position == Position.LEFT) R.attr.itemBgMe else R.attr.itemBgOther
            )
        )

        city.text = item.getSpannableString(typedValue.getColorOrThrow(0))

        (this as LinearLayout).gravity = if (item.position == Position.LEFT) Gravity.START else Gravity.END

        itemContainer.setBackgroundResource(typedValue.getResourceIdOrThrow(1))

        if (!item.isMessage)
            when (item.status) {
                CityStatus.OK ->
                    FlagDrawablesManager.getBitmapFor(context, item.countryCode)?.run {
                        wordIcon.setImageBitmap(this)
                    }
                CityStatus.WAITING ->
                    wordIcon.setImageResource(R.drawable.ic_waiting)
                CityStatus.ERROR ->
                    wordIcon.setImageResource(R.drawable.ic_word_error)
            }
        wordIcon.visibility = if (item.isMessage) View.GONE else View.VISIBLE

        typedValue.recycle()
    }

    private fun convertToPx(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()

}