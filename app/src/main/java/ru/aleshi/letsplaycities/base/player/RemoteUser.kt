package ru.aleshi.letsplaycities.base.player

import android.content.Context
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.quandastudio.lpsclient.model.PlayerData


class RemoteUser(context: Context, playerData: PlayerData) :
    User(PictureSource(context), playerData) {

    override fun onBeginMove(firstChar: Char?) {
        // Word broadcasts by NetworkServer
    }


}