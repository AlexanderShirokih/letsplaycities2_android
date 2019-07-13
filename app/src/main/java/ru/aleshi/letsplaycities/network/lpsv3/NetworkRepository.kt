package ru.aleshi.letsplaycities.network.lpsv3

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.network.PlayerData

class NetworkRepository(private val mNetworkClient: NetworkClient2) {

    private fun networkClient(): Single<NetworkClient2> =
        Single.just(mNetworkClient)
            .subscribeOn(Schedulers.io())
            .doOnSuccess { checkConnection() }

    fun login(userData: PlayerData): Single<NetworkClient2.AuthResult> {
        return networkClient()
            .map { it.login(userData) }
    }

    fun play(isWaiting: Boolean, friendId: Int?): Single<Pair<PlayerData, Boolean>> {
        return networkClient()
            .map { it.play(isWaiting, friendId) }
    }

    fun getFriendsList(): Single<ArrayList<FriendsInfo>> {
        return networkClient()
            .map { it.getFriendsList() }
    }

    fun deleteFriend(userId: Int): Completable {
        return Completable.fromRunnable {
            mNetworkClient.deleteFriend(userId)
        }
            .subscribeOn(Schedulers.io())
    }

    fun disconnect() {
        Completable.fromRunnable(mNetworkClient::disconnect)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    private fun checkConnection() {
        if (!mNetworkClient.isConnected()) {
            mNetworkClient.disconnect()
            mNetworkClient.connect()
        }
    }

    fun sendFirebaseToken(token: String) {
        networkClient()
            .doOnSuccess { it.sendFireBaseToken(token) }
            .subscribe()
    }
}