package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.PlayerData

class NetworkUser(playerData: PlayerData, picasso: Picasso) : User(
    playerData,
    PictureSource(
        picasso,
        Utils.getPictureUri(
            playerData.authData.credentials.userId,
            playerData.pictureHash
        ),
        R.drawable.ic_player_big
    )
) {

    override fun onBeginMove(firstChar: Char?) {
        // Word broadcasts by NetworkServer
    }

    override fun needsShowMenu(): Boolean = true
}