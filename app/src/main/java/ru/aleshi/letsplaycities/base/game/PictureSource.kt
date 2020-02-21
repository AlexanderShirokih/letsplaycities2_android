package ru.aleshi.letsplaycities.base.game

import android.net.Uri
import androidx.annotation.DrawableRes
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import ru.aleshi.letsplaycities.R

open class PictureSource(
    val imageRequest: RequestCreator
) {
    constructor(picasso: Picasso) : this(picasso, R.drawable.ic_player_big)

    constructor(picasso: Picasso, @DrawableRes placeholder: Int) : this(picasso.load(placeholder))

    constructor(picasso: Picasso, uri: Uri, @DrawableRes placeholder: Int) : this(picasso.run {
        if (uri == Uri.EMPTY)
            load(placeholder)
        else
            load(uri)
    }
        .placeholder(placeholder)
        .error(placeholder))
}