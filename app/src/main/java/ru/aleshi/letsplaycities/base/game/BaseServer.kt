package ru.aleshi.letsplaycities.base.game

import io.reactivex.Single

abstract class BaseServer {

    abstract fun broadcastResult(city: String): Single<WordResult>

    abstract fun getTimeLimit(): Long
}