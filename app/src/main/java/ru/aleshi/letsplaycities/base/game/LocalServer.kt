package ru.aleshi.letsplaycities.base.game

import io.reactivex.Single

class LocalServer : BaseServer() {

    override fun broadcastResult(city: String): Single<WordResult> {
        // We trust our local users
        return Single.just(WordResult.ACCEPTED)
    }

}