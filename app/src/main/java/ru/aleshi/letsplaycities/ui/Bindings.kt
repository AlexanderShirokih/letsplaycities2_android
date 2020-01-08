package ru.aleshi.letsplaycities.ui

import android.content.Context
import android.util.TypedValue
import androidx.databinding.BindingAdapter
import com.mikhaellopez.circularimageview.CircularImageView
import ru.aleshi.letsplaycities.R

object Bindings {

    @BindingAdapter("useBorder")
    @JvmStatic
    fun setUseBorder(circularImageView: CircularImageView, useBorder: Boolean) {
        val color = resolveColor(
            circularImageView.context,
            if (useBorder) R.attr.avatarBorderColor else R.attr.colorOnPrimary
        )
        circularImageView.borderColor = color
        circularImageView.shadowColor = color
    }

    private fun resolveColor(context: Context, attr: Int): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(attr, outValue, true)
        return outValue.data
    }
}