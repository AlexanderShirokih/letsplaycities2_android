package ru.aleshi.letsplaycities.network

import androidx.lifecycle.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.*
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.PlayerData
import javax.inject.Inject

class NetworkPresenterImpl @Inject constructor(
    private val mGameSessionBuilder: GameSession.GameSessionBuilder,
    private val mNetworkServer: NetworkServer,
    private val mNetworkRepository: NetworkRepository,
    private val mAuthDataFactory: GameAuthDataFactory,
    private val mGamePlayerDataFactory: GamePlayerDataFactory
) : NetworkContract.Presenter {

    private lateinit var mAuthData: AuthData
    private val mDisposable: CompositeDisposable = CompositeDisposable()
    private var mView: NetworkContract.View? = null
    private val mSaveProvider: GameAuthDataFactory.GameSaveProvider by lazy {
        GameAuthDataFactory.GameSaveProvider(
            mView!!.getGamePreferences()
        )
    }

    override fun onAttachView(view: NetworkContract.View) {
        mView = view
        val prefs = view.getGamePreferences()
        val isLoggedIn = prefs.isLoggedFromAnySN()
        if (isLoggedIn) {
            mAuthData = mAuthDataFactory.loadFromPreferences(prefs)
        }
        view.setupLayout(isLoggedIn)
        disconnect()
    }

    override fun onConnectToFriendGame(versionInfo: Pair<String, Int>, oppId: Int) {
        createPlayerData(versionInfo) {
            startFriendGame(it, oppId)
        }
    }

    override fun onConnect(versionInfo: Pair<String, Int>) {
        createPlayerData(versionInfo) {
            startGame(it, null)
        }
    }

    override fun onFriendsInfo(versionInfo: Pair<String, Int>): Observer<in FriendInfo> {
        return Observer { friendInfo ->
            friendInfo?.run {
                createPlayerData(versionInfo) { playerData ->
                    startGame(playerData, friendInfo)
                }
            }
        }
    }

    private fun disconnect() {
        onDispose()
        mNetworkRepository.disconnect()
    }


    override fun onCancel() {
        disconnect()
        mView?.onCancel()
    }

    override fun onDispose() {
        mDisposable.clear()
    }

    private fun createPlayerData(
        versionInfo: Pair<String, Int>,
        callback: (playerData: PlayerData) -> Unit
    ) {
        mView?.let {
            NetworkUtils.createPlayerData(
                versionInfo,
                callback,
                it.getGamePreferences(),
                mGamePlayerDataFactory,
                mAuthData
            )
        }
    }

    private fun startFriendGame(userData: PlayerData, oppId: Int) {
        mDisposable.add(
            mNetworkRepository.login(userData)
                .doOnSuccess { mView?.updateInfo(R.string.waiting_for_friend) }
                .flatMapCompletable { mNetworkRepository.sendFriendRequestResult(true, oppId) }
                .toSingleDefault(Unit)
                .flatMapMaybe { mNetworkRepository.connectToFriend() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ play(userData, oppData = it.first, youStarter = it.second) }, {
                    onCancel()
                    mView?.handleError(it)
                }, ::onCancel)
        )
    }

    private fun startGame(userData: PlayerData, friendsInfo: FriendInfo?) {
        mView?.checkForWaiting {
            mDisposable.add(mNetworkRepository.login(userData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    mView!!.updateInfo(R.string.connecting_to_server)
                }
                .doOnSuccess {
                    mView?.let { view ->
                        view.updateInfo(R.string.waiting_for_opp)
                        mSaveProvider.save(it.authData)
                        if (it.newerBuild > userData.clientBuild)
                            view.notifyAboutUpdates()
                    }
                }
                .observeOn(Schedulers.io())
                .flatMapMaybe {
                    mNetworkRepository.play(friendsInfo != null, friendsInfo?.userId)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ play(userData, it.first, it.second) }, {
                    onCancel()
                    mView?.handleError(it)
                }, ::onCancel)
            )
        }
    }

    private fun play(playerData: PlayerData, oppData: PlayerData, youStarter: Boolean) {
        val users = arrayOf(
            if (mNetworkRepository.isLocal) RemoteUser(oppData) else NetworkUser(oppData),
            Player(playerData)
        ).apply {
            if (youStarter)
                reverse()
        }

        mView?.onStartGame(
            mGameSessionBuilder
                .server(mNetworkServer)
                .users(users)
                .build()
        )
    }

}