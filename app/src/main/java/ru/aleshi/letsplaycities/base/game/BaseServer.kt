package ru.aleshi.letsplaycities.base.game

import io.reactivex.Observable

abstract class BaseServer {

    abstract fun getWordsResult(): Observable<Pair<WordResult, String>>

    open fun getInputMessages(): Observable<String> = Observable.empty()

    open fun broadcastMessage(message: String) = Unit

    abstract fun broadcastResult(city: String)

    abstract fun getTimeLimit(): Long
}