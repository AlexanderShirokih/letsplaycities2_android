package ru.aleshi.letsplaycities.network

import io.reactivex.Observable
import ru.aleshi.letsplaycities.base.game.BaseServer
import ru.aleshi.letsplaycities.base.game.WordResult
import ru.aleshi.letsplaycities.network.lpsv3.NetworkRepository

class NetworkServer(private val mNetworkRepository: NetworkRepository) : BaseServer() {

    override fun getWordsResult(): Observable<Pair<WordResult, String>> {
        return mNetworkRepository.words.map { it.result to it.word }
    }

    companion object {
        private const val NETWORK_TIMER = 92L
    }

    override fun broadcastResult(city: String) {
        mNetworkRepository.sendWord(city)
    }

    override fun getTimeLimit(): Long = NETWORK_TIMER
}