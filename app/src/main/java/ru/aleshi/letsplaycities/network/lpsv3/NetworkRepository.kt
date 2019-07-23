package ru.aleshi.letsplaycities.network.lpsv3

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.base.player.PlayerData
import ru.aleshi.letsplaycities.network.NetworkUtils
import java.util.concurrent.TimeUnit

class NetworkRepository(private val mNetworkClient: NetworkClient /*, errorHandler: (t: Throwable) -> Unit */) {

    private val disposable = CompositeDisposable()

    private val inputMessage: Observable<LPSMessage> = Observable.create<LPSMessage> {
        while (!it.isDisposed && mNetworkClient.isConnected()) {
            it.onNext(mNetworkClient.readMessage())
        }
        it.onComplete()
    }
        .subscribeOn(Schedulers.io())
        .doOnNext {
            Log.d("TAG", "Input message= $it")
        }
        .publish().refCount(1, TimeUnit.SECONDS)

    val words: Observable<LPSMessage.LPSWordMessage> =
        inputMessage.filter { it is LPSMessage.LPSWordMessage }.cast(LPSMessage.LPSWordMessage::class.java)

    val messages: Observable<LPSMessage.LPSMsgMessage> =
        inputMessage.filter { it is LPSMessage.LPSMsgMessage }.cast(LPSMessage.LPSMsgMessage::class.java)

    val leave: Single<LPSMessage.LPSLeaveMessage> =
        inputMessage.filter { it is LPSMessage.LPSLeaveMessage }.cast(LPSMessage.LPSLeaveMessage::class.java)
            .firstOrError()

    val timeout: Completable =
        inputMessage.filter { it is LPSMessage.LPSTimeoutMessage }.firstOrError().ignoreElement()

    val friendsRequest: Observable<LPSMessage.FriendRequest> =
        inputMessage.filter { it is LPSMessage.LPSFriendRequest }.cast(LPSMessage.LPSFriendRequest::class.java)
            .map { it.requestResult }

    val firebaseToken: Observable<Unit> =
        inputMessage.filter { it is LPSMessage.LPSRequestFirebaseToken }
            .switchMap {
                networkClient().toObservable().zipWith(
                    Observable.create<String> {
                        updateToken { token -> it.onNext(token); it.onComplete() }
                    },
                    BiFunction<NetworkClient, String, Pair<NetworkClient, String>> { client, token -> client to token })

            }
            .observeOn(Schedulers.io())
            .doOnNext { it.first.sendFirebaseToken(it.second) }
            .map { Unit }

    private fun networkClient(): Single<NetworkClient> =
        Single.just(mNetworkClient)
            .subscribeOn(Schedulers.io())
            .doOnSuccess { checkConnection() }

    fun login(userData: PlayerData): Single<NetworkClient.AuthResult> {
        return networkClient()
            .map { it.login(userData) }
    }

    fun play(isWaiting: Boolean, friendId: Int?): Single<Pair<PlayerData, Boolean>> {
        disposable.add(networkClient()
            .subscribe { client -> client.play(isWaiting, friendId) })
        return inputMessage.filter { it is LPSMessage.LPSPlayMessage }
            .cast(LPSMessage.LPSPlayMessage::class.java)
            .firstOrError()
            .map { it.opponentPlayer to it.youStarter }
    }

    fun getFriendsList(): Single<ArrayList<FriendsInfo>> {
        disposable.add(networkClient().subscribe { t -> t.requestFriendsList() })
        return inputMessage.filter { it is LPSMessage.LPSFriendsList }.cast(LPSMessage.LPSFriendsList::class.java)
            .firstOrError()
            .map { it.list }
    }

    fun deleteFriend(userId: Int): Completable {
        return networkClient()
            .doOnSuccess { mNetworkClient.deleteFriend(userId) }
            .ignoreElement()
    }

    fun kick(): Completable {
        return networkClient()
            .doOnSuccess {
                it.kick()
            }.ignoreElement()
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

    private fun updateToken(callback: (token: String) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(NetworkUtils::class.java.simpleName, "FirebaseInstanceId.getInstance() failed", task.exception)
            } else {
                //174 chars
                callback(task.result!!.token)
            }
        }
    }
}