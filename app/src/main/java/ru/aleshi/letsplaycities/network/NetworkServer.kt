package ru.aleshi.letsplaycities.network

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.aleshi.letsplaycities.base.game.BaseServer
import ru.aleshi.letsplaycities.base.game.WordResult
import ru.aleshi.letsplaycities.network.lpsv3.LPSMessage
import ru.aleshi.letsplaycities.network.lpsv3.NetworkRepository

class NetworkServer(private val mNetworkRepository: NetworkRepository) : BaseServer() {

    override fun getWordsResult(): Observable<Pair<WordResult, String>> {
        return mNetworkRepository.words.map { it.result to it.word }
    }

    override fun getInputMessages(): Observable<String> {
        return mNetworkRepository.messages.map { if (it.isSystemMsg) "[СИСТЕМА] " else "" + it.message }
    }

    companion object {
        private const val NETWORK_TIMER = 92L
    }

    override fun broadcastResult(city: String) {
        mNetworkRepository.sendWord(city)
    }

    override fun getTimeLimit(): Long = NETWORK_TIMER

    override fun broadcastMessage(message: String) =
        mNetworkRepository.sendMessage(message)

    override val leave: Single<Boolean> = mNetworkRepository.leave.map { it.leaved }

    override val timeout: Completable = mNetworkRepository.timeout

    override val friendsRequest: Observable<LPSMessage.FriendRequest> = mNetworkRepository.friendsRequest

    override fun sendFriendRequest() {
        mNetworkRepository.sendFriendRequest()
    }

    override fun sendFriendAcceptance(accepted: Boolean) {
        mNetworkRepository.sendFriendAcceptance(accepted)
    }
}