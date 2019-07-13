package ru.aleshi.letsplaycities.base.player

import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.AuthData
import ru.aleshi.letsplaycities.utils.Utils

class Android(name: String) : User(AuthData.create(name)) {

    override fun onBeginMove(firstChar: Char?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAvatar(): Maybe<Drawable> {
        return Maybe.fromCallable { Utils.loadDrawable(gameSession.context, R.drawable.ic_android_big) }
    }

}