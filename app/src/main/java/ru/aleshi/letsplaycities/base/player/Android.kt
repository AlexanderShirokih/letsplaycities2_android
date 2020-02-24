package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo
import java.util.concurrent.TimeUnit

/**
 * Represents logic of Android player.
 * @param picasso [Picasso] instance
 * @param versionInfo application [VersionInfo] instance
 * @param name localized Android name
 */
class Android(picasso: Picasso, name: String, versionInfo: VersionInfo) :
    User(
        PlayerData.SimpleFactory().create(name, versionInfo),
        PictureSource(picasso, R.drawable.ic_android_big)
    ) {

    /**
     * Count of moves before Android surrenders
     */
    private var estimatedMoves: Int = 1

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
     * @return [Maybe] word or [Maybe.empty] if no words left for this letter
     */
    override fun onMakeMove(firstChar: Char): Maybe<String> {
        return Maybe.just(firstChar)
            .delay(1500, TimeUnit.MILLISECONDS)
            .filter { estimatedMoves-- > 0 }
            .flatMap(game::getRandomWord)
    }

}