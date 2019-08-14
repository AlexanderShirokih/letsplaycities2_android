package ru.aleshi.letsplaycities.ui.game

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.utils.StringUtils

class GameItem(
    val content: String,
    val position: Position,
    var status: CityStatus = CityStatus.OK,
    val isMessage: Boolean = false,
    val countryCode: Short = 0
) {
    constructor(content: String, countryCode: Short, position: Position) : this(content, position, CityStatus.OK, false, countryCode)

    fun getSpannableString(foregroundSpanColor: Int): CharSequence? {
        return if (isMessage)
            SpannableStringBuilder(content)
        else {
            val end = content.lastIndexOf(StringUtils.findLastSuitableChar(content.toLowerCase()) ?: 0.toChar())
            SpannableStringBuilder(StringUtils.toTitleCase(content)).apply {
                setSpan(
                    ForegroundColorSpan(foregroundSpanColor),
                    0,
                    1,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(foregroundSpanColor),
                    end,
                    end + 1,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}