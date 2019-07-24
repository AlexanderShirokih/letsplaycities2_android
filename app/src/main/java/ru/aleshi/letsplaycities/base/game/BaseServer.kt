package ru.aleshi.letsplaycities.base.game

/**/
import io.reactivex.Maybe
import io.reactivex.Observable
import ru.aleshi.letsplaycities.network.lpsv3.LPSMessage

abstract class BaseServer {

    abstract fun getWordsResult(): Observable<Pair<WordResult, String>>

    open fun getInputMessages(): Observable<String> = Observable.empty()

    open fun broadcastMessage(message: String) = Unit

    abstract fun broadcastResult(city: String)

    abstract fun getTimeLimit(): Long

    open fun sendFriendRequest() = Unit

    open fun sendFriendAcceptance(accepted: Boolean) = Unit
    open fun dispose() = Unit

    open fun banUser(userId: Int) = Unit

    open val leave: Maybe<Boolean> = Maybe.never()

    open val timeout: Maybe<LPSMessage> = Maybe.never()

    open val friendsRequest: Observable<LPSMessage.FriendRequest> = Observable.never()

    open val kick: Maybe<Boolean> = Maybe.never()
}