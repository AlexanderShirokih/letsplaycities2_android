package ru.aleshi.letsplaycities.base.player

import ru.aleshi.letsplaycities.base.GameSession
import ru.aleshi.letsplaycities.social.AuthData

class NetworkUser(gameSession: GameSession, authData: AuthData): User(gameSession, authData) {
    override fun onBeginMove(firstChar: Char?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}