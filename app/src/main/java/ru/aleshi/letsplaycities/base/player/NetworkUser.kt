package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import io.reactivex.Observable
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.PlayerData

/**
 * Represents logic of network player. This is a remote bridge,
 * and on the other side it represented by [Player].
 * @param server [BaseServer] network server instance
 * @param playerData [PlayerData] model class that contains info about user
 * @param picasso Picasso instance
 */
class NetworkUser(private val server: BaseServer, playerData: PlayerData, picasso: Picasso) : User(
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

    override fun onMakeMove(firstChar: Char): Observable<ResultWithCity> =
        server.getIncomingWords().filter { it.identity.isTheSameUser(this) }

}