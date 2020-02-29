package ru.aleshi.letsplaycities.base.game

import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import ru.aleshi.letsplaycities.R
import java.net.URI

open class PictureSource(
    val imageRequest: RequestCreator
) {
    constructor(picasso: Picasso) : this(picasso, R.drawable.ic_player_big)

    constructor(picasso: Picasso, @DrawableRes placeholder: Int) : this(picasso.load(placeholder))

    constructor(
        picasso: Picasso,
        uri: URI?, @DrawableRes placeholder: Int = R.drawable.ic_player_big
    ) : this(
        (uri?.run { picasso.load(toString().toUri()) } ?: picasso.load(placeholder))
            .placeholder(placeholder)
            .error(placeholder))
}