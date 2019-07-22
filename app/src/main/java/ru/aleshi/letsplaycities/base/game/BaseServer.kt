package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

abstract class BaseServer {

    abstract fun getWordsResult(): Observable<Pair<WordResult, String>>

    open fun getInputMessages(): Observable<String> = Observable.empty()

    open fun broadcastMessage(message: String) = Unit

    abstract fun broadcastResult(city: String)

    abstract fun getTimeLimit(): Long

    open fun sendFriendRequest() = Unit

    open val leave: Single<Boolean> = Single.never()

    open val timeout: Completable = Completable.never()
}