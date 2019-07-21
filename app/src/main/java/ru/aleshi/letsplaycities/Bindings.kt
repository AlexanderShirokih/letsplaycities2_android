package ru.aleshi.letsplaycities

import android.content.Context
import android.util.TypedValue
import androidx.databinding.BindingAdapter
import com.mikhaellopez.circularimageview.CircularImageView

object Bindings {

    @BindingAdapter("app:useBorder")
    @JvmStatic
    fun setUseBorder(circularImageView: CircularImageView, useBorder: Boolean) {
        val color = resolveColor(
            circularImageView.context,
            if (useBorder) R.attr.colorPrimaryDark else R.attr.colorOnPrimary
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