package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.PlayerData

/**
 * Represents logic of remote network player. This is a remote bridge,
 * and on the other side it represented by [Player].
 * @param playerData [PlayerData] model class that contains info about user
 * @param picasso Picasso instance
 */
class NetworkUser(playerData: PlayerData, picasso: Picasso) : User(
    playerData,
    PictureSource(
        picasso,
        Utils.getPictureURI(
            playerData.authData.credentials.userId,
            playerData.pictureHash
        )
    )
) {

    override fun onInit(comboSystemView: ComboSystemView): ComboSystem = ComboSystem(true)

    // Word broadcasts by NetworkServer
    override fun onMakeMove(firstChar: Char): Maybe<String> = Maybe.empty()

}