package ru.aleshi.letsplaycities.network

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import ru.aleshi.letsplaycities.base.game.BaseServer
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.WordResult
import javax.inject.Inject

class NetworkServer @Inject constructor(private val mNetworkRepository: NetworkRepository) :
    BaseServer() {

    override fun getWordsResult(): Observable<Pair<WordResult, String>> {
        return mNetworkRepository.getWords().map { it.result to it.word }
    }

    override fun getInputMessages(): Observable<String> {
        return mNetworkRepository.getMessages()
            .map { if (it.isSystemMsg) "[СИСТЕМА] " else "" + it.msg }
    }

    companion object {
        private const val NETWORK_TIMER = 92L
    }

    override fun dispose() {
        mNetworkRepository.disconnect()
    }

    override fun broadcastResult(city: String) =
        mNetworkRepository.sendWord(city)

    override fun getTimeLimit(): Long = NETWORK_TIMER

    override fun broadcastMessage(message: String): Completable =
        mNetworkRepository.sendMessage(message)

    override val leave: Maybe<Boolean> = mNetworkRepository.getLeave().map { it.leaved }

    override val timeout: Maybe<LPSMessage> = mNetworkRepository.getTimeout()

    override val friendsRequest: Observable<LPSMessage.FriendRequest> =
        mNetworkRepository.getFriendsRequest()

    override fun sendFriendRequest(): Completable =
        mNetworkRepository.sendFriendRequest()

    override fun sendFriendAcceptance(accepted: Boolean): Completable =
        mNetworkRepository.sendFriendAcceptance(accepted)

    override fun banUser(userId: Int): Completable =
        mNetworkRepository.banUser(userId)


    override val kick: Maybe<Boolean> = mNetworkRepository.getKick().map { it.isBannedBySystem }
}