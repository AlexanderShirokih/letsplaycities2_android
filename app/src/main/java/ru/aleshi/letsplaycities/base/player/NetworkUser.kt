package ru.aleshi.letsplaycities.base.player

import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import ru.aleshi.letsplaycities.base.AuthData

class NetworkUser(authData: AuthData): User(authData) {

    override fun getAvatar(): Maybe<Drawable> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBeginMove(firstChar: Char?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}