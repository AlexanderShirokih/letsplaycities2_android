package ru.aleshi.letsplaycities.base.player

import io.reactivex.Completable
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.quandastudio.lpsclient.model.PlayerData

abstract class User(
    internal val pictureSource: PictureSource,
    internal val playerData: PlayerData,
    internal val hasUserInput: Boolean = false,
    internal val canUseQuickTime: Boolean = true
) {

    lateinit var gameSession: GameSession
    lateinit var comboSystem: ComboSystem

    var score: Int = 0

    var position: Position = Position.UNKNOWN

    val name: String
        get() = playerData.authData.login

    val info: String
        get() = if (score == 0) name else "$name: $score"

    abstract fun onBeginMove(firstChar: Char?)

    fun sendCity(city: String): Completable =
        gameSession.onCitySent(city, this)

    open fun onUserInput(userInput: String, onSuccess: () -> Unit) = Unit

    open fun reset() {
        score = 0
    }

    fun isMessagesAllowed() = playerData.canReceiveMessages

    open fun needsShowMenu() = false

    fun increaseScore(points: Int) {
        score += (points * comboSystem.multiplier).toInt()
    }
}