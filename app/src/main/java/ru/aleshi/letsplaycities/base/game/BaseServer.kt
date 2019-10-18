package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.WordResult

abstract class BaseServer {

    abstract fun getWordsResult(): Observable<Pair<WordResult, String>>

    open fun getInputMessages(): Observable<String> = Observable.empty()

    open fun broadcastMessage(message: String): Completable = Completable.complete()

    abstract fun broadcastResult(city: String) : Completable

    abstract fun getTimeLimit(): Long

    open fun sendFriendRequest(): Completable = Completable.complete()

    open fun sendFriendAcceptance(accepted: Boolean): Completable = Completable.complete()
    open fun dispose() = Unit

    open fun banUser(userId: Int): Completable = Completable.complete()

    open val leave: Maybe<Boolean> = Maybe.never()

    open val timeout: Maybe<LPSMessage> = Maybe.never()

    open val friendsRequest: Observable<LPSMessage.FriendRequest> = Observable.never()

    open val kick: Maybe<Boolean> = Maybe.never()
}