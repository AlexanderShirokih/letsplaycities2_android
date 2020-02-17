package ru.aleshi.letsplaycities.base.player

import android.content.res.Resources
import com.squareup.picasso.Picasso
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.PicassoPictureSource
import ru.quandastudio.lpsclient.model.PlayerData

class NetworkUser(resources: Resources, playerData: PlayerData, picasso: Picasso) : User(
    PicassoPictureSource(
        resources,
        picasso,
        playerData.authData.credentials.userId,
        playerData.pictureHash,
        R.drawable.ic_player_big
    ), playerData
) {

    override fun onBeginMove(firstChar: Char?) {
        // Word broadcasts by NetworkServer
    }

    override fun needsShowMenu(): Boolean = true
}