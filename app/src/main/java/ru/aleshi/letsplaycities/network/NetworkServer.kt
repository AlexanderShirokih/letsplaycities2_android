package ru.aleshi.letsplaycities.network

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.player.UserIdIdentity
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.base.server.ResultWithMessage
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.core.LPSMessage
import javax.inject.Inject

class NetworkServer @Inject constructor(private val mNetworkRepository: NetworkRepository) :
    BaseServer() {

    override fun getWordsResult(): Observable<ResultWithCity> {
        return mNetworkRepository.getWords().map {
            ResultWithCity(
                wordResult = it.result,
                city = it.word,
                identity = UserIdIdentity(it.ownerId)
            )
        }
    }

    override fun getIncomingMessages(): Observable<ResultWithMessage> {
        return mNetworkRepository.getMessages()
            .map {
                ResultWithMessage(
                    message = if (it.isSystemMsg) "[СИСТЕМА] " else "" + it.msg,
                    identity = UserIdIdentity(it.ownerId)
                )
            }
    }

    companion object {
        private const val NETWORK_TIMER = 92L
    }

    override fun dispose() {
        mNetworkRepository.disconnect()
    }

    override fun sendCity(city: String, sender: User): Completable =
        mNetworkRepository.sendWord(city)

    override fun getTimeLimit(): Long = NETWORK_TIMER

    override fun sendMessage(message: String, sender: User): Completable =
        mNetworkRepository.sendMessage(message)

    override val leave: Maybe<Boolean> = mNetworkRepository.getLeave().map { it.leaved }

    override val timeout: Maybe<LPSMessage> = mNetworkRepository.getTimeout()

    override val friendsRequest: Observable<LPSMessage.FriendRequest> =
        mNetworkRepository.getFriendsRequest()

    /**
     * Sends friends request from current player to [userId].
     */
    override fun sendFriendRequest(userId: Int): Completable =
        mNetworkRepository.sendFriendRequest(userId)

    override fun sendFriendAcceptance(accepted: Boolean, userId: Int): Completable =
        mNetworkRepository.sendFriendAcceptance(accepted, userId)

    override fun banUser(userId: Int): Completable =
        mNetworkRepository.banUser(userId)


    override val kick: Maybe<Boolean> = mNetworkRepository.getKick().map { it.isBannedBySystem }
}