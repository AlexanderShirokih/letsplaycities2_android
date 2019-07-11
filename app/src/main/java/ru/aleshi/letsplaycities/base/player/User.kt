package ru.aleshi.letsplaycities.base.player

import ru.aleshi.letsplaycities.base.GameSession
import ru.aleshi.letsplaycities.social.AuthData

abstract class User(protected val gameSession: GameSession, val authData: AuthData) {

    val name: String
        get() = authData.login

    var score: Int = 0

    abstract fun onBeginMove(firstChar: Char?)

    fun sendCity(city: String) {
        gameSession.onCitySended(city, this)
    }
}