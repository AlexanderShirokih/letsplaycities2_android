package ru.aleshi.letsplaycities.base.player

import io.reactivex.Maybe
import io.reactivex.Observable
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.base.game.SurrenderException
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
import java.util.concurrent.TimeUnit

/**
 * Represents logic of Android player.
 * @param playerData [PlayerData] model class that contains info about user
 * @param pictureSource represents android's picture
 */
class Android(playerData: PlayerData, pictureSource: PictureSource) :
    User(playerData, pictureSource) {

    /**
     * Count of moves before Android surrenders
     */
    internal var estimatedMoves: Int = 1

    override fun onInit(comboSystemView: ComboSystemView): ComboSystem {
        val diff = game.difficulty - 1
        estimatedMoves = (20 + diff.toFloat() / 3f * 70).toInt()
        estimatedMoves = (estimatedMoves..(estimatedMoves * 1.35f).toInt()).random()
        return ComboSystem(false)
    }

    /**
     * Generates random word starting with [firstChar]
     * and return it as [Maybe].
     * @param firstChar the first char of new word
     * @return [Maybe] word or [Maybe.empty] if no words left for this letter.
     * Note that [Android] doesn't checks words in server.
     */
    override fun onMakeMove(firstChar: Char): Observable<ResultWithCity> =
        Maybe.just(firstChar)
            .delay(1500, TimeUnit.MILLISECONDS)
            .filter { estimatedMoves-- > 0 }
            .flatMap(game::getRandomWord)
            .switchIfEmpty(Maybe.error(SurrenderException(this, false)))
            .map {
                ResultWithCity(
                    wordResult = WordResult.ACCEPTED,
                    identity = UserIdIdentity(this),
                    city = it
                )
            }.toObservable()

}