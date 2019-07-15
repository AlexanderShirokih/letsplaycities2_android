package ru.aleshi.letsplaycities.base.player

import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import ru.aleshi.letsplaycities.base.game.GameSession

abstract class User(private val authData: AuthData) {

    lateinit var gameSession: GameSession

    var score: Int = 0

    val name: String
        get() = authData.login

    val info: String
        get() = if (score == 0) name else "$name: $score"

    abstract fun onBeginMove(firstChar: Char?)

    fun sendCity(city: String) {
        gameSession.onCitySended(city, this)
    }

    abstract fun getAvatar(): Maybe<Drawable>
}