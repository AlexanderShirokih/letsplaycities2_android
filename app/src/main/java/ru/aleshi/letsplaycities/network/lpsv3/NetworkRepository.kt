package ru.aleshi.letsplaycities.network.lpsv3

import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.base.player.PlayerData
import ru.aleshi.letsplaycities.ui.blacklist.BlackListItem
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepository @Inject constructor(private val mNetworkClient: NetworkClient) {

    private val disposable = CompositeDisposable()

    private val inputMessage: Observable<LPSMessage> = Observable.create<LPSMessage> {
        while (!it.isDisposed && mNetworkClient.isConnected()) {
            it.onNext(mNetworkClient.readMessage())
        }
        it.onComplete()
    }
        .subscribeOn(Schedulers.io())
        .retry(3) { t ->
            mNetworkClient.disconnect()
            t is IOException
        }
        .publish().refCount(1, TimeUnit.SECONDS)

    val words: Observable<LPSMessage.LPSWordMessage> =
        inputMessage.filter { it is LPSMessage.LPSWordMessage }.cast(LPSMessage.LPSWordMessage::class.java)

    val messages: Observable<LPSMessage.LPSMsgMessage> =
        inputMessage.filter { it is LPSMessage.LPSMsgMessage }.cast(LPSMessage.LPSMsgMessage::class.java)

    val leave: Maybe<LPSMessage.LPSLeaveMessage> =
        inputMessage.filter { it is LPSMessage.LPSLeaveMessage }.cast(LPSMessage.LPSLeaveMessage::class.java)
            .firstElement()

    val timeout: Maybe<LPSMessage> =
        inputMessage.filter { it is LPSMessage.LPSTimeoutMessage }.firstElement()

    val friendsRequest: Observable<LPSMessage.FriendRequest> =
        inputMessage.filter { it is LPSMessage.LPSFriendRequest }.cast(LPSMessage.LPSFriendRequest::class.java)
            .map { it.requestResult }

    val kick: Maybe<LPSMessage.LPSBannedMessage> =
        inputMessage.filter { it is LPSMessage.LPSBannedMessage }.cast(LPSMessage.LPSBannedMessage::class.java)
            .firstElement()

    private fun networkClient(): Single<NetworkClient> =
        Single.just(mNetworkClient)
            .subscribeOn(Schedulers.io())
            .doOnSuccess { checkConnection() }

    fun login(userData: PlayerData): Single<NetworkClient.AuthResult> {
        return networkClient()
            .doOnSuccess { userData.firebaseToken = getToken().blockingGet() }
            .map { it.login(userData) }
    }

    class BannedPlayerException : Exception()

    fun play(isWaiting: Boolean, friendId: Int?): Maybe<Pair<PlayerData, Boolean>> {
        return networkClient()
            .doOnSuccess { client -> client.play(isWaiting, friendId) }
            .toObservable()
            .flatMap {
                inputMessage.filter { it is LPSMessage.LPSPlayMessage }
                    .cast(LPSMessage.LPSPlayMessage::class.java)
            }
            .flatMap {
                if (it.banned) Observable.error(BannedPlayerException()) else Observable.just(it)
            }
            .retry { t: Throwable -> t is BannedPlayerException }
            .map { it.opponentPlayer to it.youStarter }
            .firstElement()
    }

    fun getBlackList(): Single<ArrayList<BlackListItem>> {
        return networkClient()
            .doOnSuccess { t -> t.requestBlackList() }
            .toObservable()
            .flatMap {
                inputMessage.filter { it is LPSMessage.LPSBannedListMessage }
                    .cast(LPSMessage.LPSBannedListMessage::class.java)
            }
            .firstOrError()
            .map { it.list }
    }

    fun getFriendsList(): Single<ArrayList<FriendsInfo>> {
        return networkClient()
            .doOnSuccess { t -> t.requestFriendsList() }
            .toObservable()
            .flatMap {
                inputMessage.filter { it is LPSMessage.LPSFriendsList }.cast(LPSMessage.LPSFriendsList::class.java)
            }
            .firstOrError()
            .map { it.list }
    }

    fun deleteFriend(userId: Int): Completable {
        return networkClient()
            .doOnSuccess { mNetworkClient.deleteFriend(userId) }
            .ignoreElement()
    }


    fun removeFromBanList(userId: Int): Completable {
        return networkClient()
            .doOnSuccess { mNetworkClient.removeFromBanList(userId) }
            .ignoreElement()
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

    fun sendFriendRequestResult(result: Boolean, userId: Int): Completable {
        return networkClient()
            .subscribeOn(Schedulers.io())
            .doOnSuccess { it.sendFriendRequestResult(result, userId) }
            .ignoreElement()
    }

    fun sendWord(city: String) {
        disposable.add(networkClient()
            .subscribe { client -> client.sendWord(city) })
    }

    fun sendMessage(message: String) {
        disposable.add(networkClient()
            .subscribe { client -> client.sendMessage(message) })
    }

    fun sendFriendRequest() {
        disposable.add(networkClient().subscribe { client -> client.sendFriendRequest() })
    }

    fun sendFriendAcceptance(accepted: Boolean) {
        disposable.add(networkClient().subscribe { client -> client.sendFriendAcceptance(accepted) })
    }

    private fun getToken(): Single<String> {
        return Single.create {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                //174 chars
                if (task.isSuccessful)
                    it.onSuccess(task.result!!.token)
                else it.onError(LPSException("Cannot get firebase token"))
            }
        }

    }

    fun banUser(userId: Int) {
        disposable.add(networkClient().subscribe { client -> client.banUser(userId) })
    }

}