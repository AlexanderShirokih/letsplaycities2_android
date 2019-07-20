package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.StringUtils

class GameItem(
    val content: String,
    val isLeft: Boolean,
    var status: CityStatus = CityStatus.OK,
    val isMessage: Boolean = false,
    val countryCode: Short = 0
) {

    fun getSpannableString(context: Context): CharSequence? {
        return if (isMessage)
            SpannableStringBuilder(content)
        else {
            val foregroundSpanColor: Int = resolveForegroundColor(context)
            val end = content.lastIndexOf(StringUtils.findLastSuitableChar(content.toLowerCase()) ?: 0.toChar())
            SpannableStringBuilder(StringUtils.firstToUpper(content)).apply {
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

    private fun resolveForegroundColor(context: Context): Int {
        val out = TypedValue()
        context.theme.resolveAttribute(R.attr.fgSpanColor, out, true)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            context.resources.getColor(out.resourceId, context.theme)
        else context.resources.getColor(out.resourceId)
    }

}