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

    /**
     * Called right after user view was created.
     */
    override fun onAttachView(view: NetworkContract.View) {
        mView = view
        val prefs = view.getGamePreferences()
        val isLoggedIn = prefs.isLoggedFromAnySN()
        if (isLoggedIn) {
            mAuthData = mAuthDataFactory.loadFromPreferences(prefs)
        }
        view.setupLayout(isLoggedIn)
    }

    /**
     * Called when received request to start game in friend mode.
     */
    override fun onConnectToFriendGame(versionInfo: Pair<String, Int>, oppId: Int) {
        createPlayerData(versionInfo) {
            startFriendGame(it, oppId)
        }
    }

    /**
     * Called when user touched connect button.
     */
    override fun onConnect(versionInfo: Pair<String, Int>) {
        createPlayerData(versionInfo) {
            startGame(it, null)
        }
    }

    /**
     * Called when user wants to start game in friend mode.
     */
    override fun onFriendsInfo(versionInfo: Pair<String, Int>): Observer<in FriendInfo> {
        return Observer { friendInfo ->
            friendInfo?.run {
                createPlayerData(versionInfo) { playerData ->
                    startGame(playerData, friendInfo)
                }
            }
        }
    }

    /**
     * This function is called when user wants to cancel connection,
     * either an error or another problems was occurred in login sequence.
     */
    override fun onCancel() {
        mNetworkRepository.disconnect()
        onDispose()
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
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { mView?.updateInfo(R.string.waiting_for_friend) }
                .observeOn(Schedulers.io())
                .doOnNext { println("Ready to send request") }
                .flatMap { mNetworkRepository.sendFriendRequestResult(true, oppId) }
                .doOnComplete { println("Sended!") }
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
                .doOnNext {
                    mView?.let { view ->
                        view.updateInfo(R.string.waiting_for_opp)
                        mSaveProvider.save(it.authData)
                        if (it.newerBuild > userData.clientBuild)
                            view.notifyAboutUpdates()
                    }
                }
                .observeOn(Schedulers.io())
                .flatMap {
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