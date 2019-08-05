package ru.aleshi.letsplaycities.base.player

import android.content.Context
import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import ru.quandastudio.lpsclient.model.PlayerData


class RemoteUser(playerData: PlayerData) : User(playerData) {

    override fun getAvatar(context: Context): Maybe<Drawable> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBeginMove(firstChar: Char?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}