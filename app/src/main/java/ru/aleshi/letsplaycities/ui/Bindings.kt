package ru.aleshi.letsplaycities.ui

import android.content.Context
import android.net.Uri
import android.util.TypedValue
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
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

    @BindingAdapter("playerImageUri")
    @JvmStatic
    fun setPlayerImageUri(imageView: ImageView, playerImageUri: Uri) {
        Picasso.get()
            .load(if (playerImageUri == Uri.EMPTY) null else playerImageUri)
            .placeholder(R.drawable.ic_player_big)
            .error(R.drawable.ic_player_big
            )
            .networkPolicy(NetworkPolicy.NO_CACHE)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .into(imageView)
    }

    @BindingAdapter("imageRequest")
    @JvmStatic
    fun setPlayerImageRequest(imageView: ImageView, imageRequest: RequestCreator?) {
        imageRequest?.into(imageView)
    }

}