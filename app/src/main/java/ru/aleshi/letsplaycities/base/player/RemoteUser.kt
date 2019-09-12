package ru.aleshi.letsplaycities.base.player

import android.content.Context
import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.PlayerData


class RemoteUser(playerData: PlayerData) : User(playerData) {

    override fun getAvatar(context: Context): Maybe<Drawable> =
        Utils.loadAvatar(context, playerData.avatar, R.drawable.ic_player_big)


    override fun onBeginMove(firstChar: Char?) {
        // Word broadcasts by NetworkServer
    }


}