package ru.aleshi.letsplaycities.base.game

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.reactivex.Observable
import ru.aleshi.letsplaycities.utils.Utils

class PicassoPictureSource(
    resources: Resources,
    picasso: Picasso,
    path: Uri?, @DrawableRes placeholder: Int,
    noCache: Boolean = false,
    fitSize: Boolean = false
) :

    PictureSource(
        Observable.create<Drawable> { emitter ->
            picasso.run {
                if (path == null)
                    load(placeholder)
                else
                    load(path)
            }
                .placeholder(placeholder)
                .error(placeholder)
                .apply {
                    if (noCache)
                        networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                    if (fitSize)
                        resize(0, 128)
                }
                .into(object : Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        placeHolderDrawable?.run {
                            emitter.onNext(this)
                        }
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                        errorDrawable?.run {
                            emitter.onNext(this)
                        }
                        emitter.onComplete()
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
                        emitter.onNext(BitmapDrawable(resources, bitmap))
                        emitter.onComplete()
                    }
                })
        }) {
    /* TODO:
     * Check avatar loading in network mode
     */
    constructor(
        resources: Resources,
        picasso: Picasso,
        userId: Int, pictureHash: String?, @DrawableRes placeholder: Int
    ) : this(
        resources,
        picasso,
        Uri.parse(Utils.getPictureUrl(userId, pictureHash)),
        placeholder
    )

}
