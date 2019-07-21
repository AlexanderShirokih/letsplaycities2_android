package ru.aleshi.letsplaycities.base.game

import io.reactivex.Single
import ru.aleshi.letsplaycities.base.GamePreferences

class LocalServer(gamePreferences: GamePreferences) : BaseServer() {
    private val timeLimit = gamePreferences.getTimeLimit()

    override fun broadcastResult(city: String): Single<WordResult> {
        // We trust our local users
        return Single.just(WordResult.ACCEPTED)
    }

    override fun getTimeLimit(): Long = timeLimit

}