package ru.aleshi.letsplaycities.network

import androidx.lifecycle.Observer
import io.reactivex.Observable
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

/**
 * This class handles onConnect events and initiates login process
 */
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
     * Saves view reference and call setupLayout
     * @param view view to be attached
     * @see NetworkContract.View.setupLayout
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
     * @param versionInfo version of the application(Pair<versionName, versionCode>)
     * @param oppId ID of the opponent you want to play with
     */
    override fun onConnectToFriendGame(versionInfo: Pair<String, Int>, oppId: Int) {
        createPlayerData(versionInfo) {
            startFriendGame(it, oppId)
        }
    }

    /**
     * Called when user touched connect button.
     * @param versionInfo version of the application(Pair<versionName, versionCode>)
     */
    override fun onConnect(versionInfo: Pair<String, Int>) {
        createPlayerData(versionInfo) {
            startGame(it, null)
        }
    }

    /**
     * Called when user wants to start game in friend mode.
     * @param versionInfo version of the application(Pair<versionName, versionCode>)
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

    /**
     * This function is called when user view destroys to dispose
     * resources and interrupt connections.
     */
    override fun onDispose() {
        mDisposable.clear()
    }

    /**
     * Creates PlayerData with given versionInfo
     * and callback which takes newly created data.
     * @param versionInfo version of the application(Pair<versionName, versionCode>)
     * @param callback callback with result
     */
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

    /**
     * Starts friend-game login sequence
     * @param userData Players data
     * @param oppId ID of the opponent you want to play with
     * @see processFriendGameLoginSequence
     */
    private fun startFriendGame(userData: PlayerData, oppId: Int) {
        mDisposable.add(
            processFriendGameLoginSequence(userData, oppId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ play(userData, oppData = it.first, youStarter = it.second) }, {
                    onCancel()
                    mView?.handleError(it)
                }, ::onCancel)
        )
    }

    /**
     * Starts game login sequence
     * @param userData Players data
     * @param friendsInfo for random pair mode should contain info about friend you want to invite
     * @see processLoginSequence
     */
    private fun startGame(userData: PlayerData, friendsInfo: FriendInfo?) {
        mView?.checkForWaiting {
            mDisposable.add(
                processLoginSequence(userData, friendsInfo)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ play(userData, it.first, it.second) }, {
                        onCancel()
                        mView?.handleError(it)
                    }, ::onCancel)
            )
        }
    }

    /**
     * This function creates friend-game login sequence.
     * @param userData Players data
     * @param oppId ID of the opponent you want to play with
     * @return Observable with result of login sequence (Pair of opponents data and who starts the
     * game (true - user, false -opponent). If something went wrong will return error.
     */
    private fun processFriendGameLoginSequence(
        userData: PlayerData,
        oppId: Int
    ): Observable<Pair<PlayerData, Boolean>> {
        return mNetworkRepository.login(userData)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { mView?.updateInfo(R.string.waiting_for_friend) }
            .observeOn(Schedulers.io())
            .flatMap { mNetworkRepository.sendFriendRequestResult(true, oppId) }
            .flatMapMaybe { mNetworkRepository.connectToFriend() }
    }

    /**
     * This function creates game login sequence.
     * @param userData Players data
     * @param friendsInfo for random pair mode should contain info about friend you want to invite
     * @return Observable with result of login sequence (Pair of opponents data and who starts the
     * game (true - user, false -opponent). If something went wrong will return error.
     */
    private fun processLoginSequence(
        userData: PlayerData,
        friendsInfo: FriendInfo?
    ): Observable<Pair<PlayerData, Boolean>> {
        return mNetworkRepository
            .login(userData)
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
    }

    /**
     * Used to create list of players and then start the game.
     * @param playerData user's player data
     * @param oppData opponent's player data
     * @param youStarter true - if user starts the game, false - if opponent
     * @see NetworkContract.View.onStartGame
     */
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