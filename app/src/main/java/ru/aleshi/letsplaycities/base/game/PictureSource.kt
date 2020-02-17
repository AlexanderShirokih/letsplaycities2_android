package ru.aleshi.letsplaycities.base.game

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import io.reactivex.Observable
import ru.aleshi.letsplaycities.R

open class PictureSource(
    val imageBitmap: Observable<Drawable>
) {
    constructor(context: Context) : this(context, R.drawable.ic_player_big)

    @Suppress("DEPRECATION")
    constructor(context: Context, @DrawableRes placeholder: Int) : this(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            Observable.just(context.resources.getDrawable(placeholder, context.theme))
        else
            Observable.just(context.resources.getDrawable(placeholder))
    )
}