package ru.aleshi.letsplaycities.network

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.player.UserIdIdentity
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.base.server.ResultWithMessage
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.core.LPSMessage
import javax.inject.Inject

class NetworkServer @Inject constructor(internal val networkRepository: NetworkRepository) :
    BaseServer({ 92L }) {

    override fun getDisconnections(): Observable<LPSMessage.LPSLeaveMessage> =
        networkRepository.getLeave().toObservable()

    override fun getIncomingWords(): Observable<ResultWithCity> {
        return networkRepository.getWords().map {
            ResultWithCity(
                wordResult = it.result,
                city = it.word,
                identity = UserIdIdentity(it.ownerId)
            )
        }
    }

    override fun getIncomingMessages(): Observable<ResultWithMessage> {
        return networkRepository.getMessages()
            .map {
                ResultWithMessage(
                    message = if (it.isSystemMsg) "[СИСТЕМА] " else "" + it.msg,
                    identity = UserIdIdentity(it.ownerId)
                )
            }
    }

    override fun dispose() {
        networkRepository.disconnect()
    }

    override fun sendCity(city: String, sender: User): Observable<ResultWithCity> {
        return Observable.zip(
            networkRepository.sendWord(city)
                .andThen(Observable.just(Unit)),
            getIncomingWords().filter { it.identity.isTheSameUser(sender) },
            BiFunction { _: Unit, word: ResultWithCity -> word }
        )
    }

    override fun getTimer(): Observable<Long> =
        super.getTimer().takeUntil(networkRepository.getTimeout().toObservable())

    override fun sendMessage(message: String, sender: User): Completable =
        networkRepository.sendMessage(message)

    override val friendRequestResult: Observable<LPSMessage.LPSFriendRequest> =
        networkRepository.getFriendsRequest()

    /**
     * Sends friends request from current player to [userId].
     */
    override fun sendFriendRequest(userId: Int): Completable =
        networkRepository.sendFriendRequest(userId)

    override fun banUser(userId: Int): Completable =
        networkRepository.banUser(userId)

    /**
     * I don't know why but isBannedBySystem inverted, so we flit it back and now `true` means
     * banned by system and `false` banned by opponent
     */
    override val kick: Maybe<Boolean> = networkRepository.getKick().map { !it.isBannedBySystem }
}