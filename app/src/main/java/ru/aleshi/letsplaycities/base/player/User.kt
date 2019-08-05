package ru.aleshi.letsplaycities.base.player

import android.content.Context
import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.game.Position
import ru.quandastudio.lpsclient.model.PlayerData

abstract class User(internal val playerData: PlayerData) {

    lateinit var gameSession: GameSession

    var score: Int = 0

    var position: Position = Position.UNKNOWN

    val name: String
        get() = playerData.userName!!

    val info: String
        get() = if (score == 0) name else "$name: $score"

    abstract fun onBeginMove(firstChar: Char?)

    fun sendCity(city: String) {
        gameSession.onCitySended(city, this)
    }

    abstract fun getAvatar(context: Context): Maybe<Drawable>

    open fun reset() {
        score = 0
    }

    fun isMessagesAllowed() = playerData.canReceiveMessages

    open fun needsShowMenu() = false
}