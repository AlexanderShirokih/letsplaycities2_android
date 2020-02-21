package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.quandastudio.lpsclient.model.PlayerData


class RemoteUser(playerData: PlayerData, picasso: Picasso) :
    User(playerData, PictureSource(picasso)) {

    override fun onBeginMove(firstChar: Char?) {
        // Word broadcasts by NetworkServer
    }


}