package ru.aleshi.letsplaycities.network

import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.AppVersionInfo
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameMode
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.base.player.NetworkUser
import ru.aleshi.letsplaycities.base.player.Player
import ru.quandastudio.lpsclient.model.*
import javax.inject.Inject

/**
 * This class handles onConnect events and initiates login process
 */
class NetworkPresenterImpl @Inject constructor(
    @AppVersionInfo
    private val versionInfo: VersionInfo,
    private val networkServer: NetworkServer,
    private val authDataFactory: GameAuthDataFactory,
    private val picasso: Picasso,
    private val gamePreferences: GamePreferences
) : NetworkContract.Presenter {

    private lateinit var authData: AuthData
    private val networkRepository = networkServer.networkRepository
    private val disposable: CompositeDisposable = CompositeDisposable()
    private var view: NetworkContract.View? = null
    private var isLocal = false

    /**
     * Called right after user view was created.
     * Saves view reference and call setupLayout
     * @param view view to be attached
     * @see NetworkContract.View.setupLayout
     */
    override fun onAttachView(view: NetworkContract.View, isLocal: Boolean) {
        this.isLocal = isLocal
        this.view = view
        authData = authDataFactory.load()
        view.setupLayout(gamePreferences.isLoggedIn(), isLocal)
    }

    /**
     * Called when received request to start game in friend mode.
     * @param oppId ID of the opponent you want to play with
     */
    override fun onConnectToFriendGame(oppId: Int) {
        startFriendGame(createPlayerData(), oppId)
    }

    /**
     * Called when user touched the connect button.
     * @param friendsInfo if not null, game will connects to friend mode.
     */
    override fun onConnect(friendsInfo: FriendInfo?) {
        startGame(createPlayerData(), friendsInfo)
    }

    /**
     * This function is called when user wants to cancel connection,
     * either an error or another problems was occurred in login sequence.
     */
    override fun onCancel() {
        networkRepository.disconnect()
        onDispose()
        view?.apply {
            onCancel()
            setupLayout(true, isLocal)
        }
    }

    /**
     * This function is called when user view destroys to dispose
     * resources and interrupt connections.
     */
    override fun onDispose() {
        disposable.clear()
    }

    /**
     * Creates new PlayerData.
     */
    private fun createPlayerData() =
        PlayerData(
            authData = authData,
            versionInfo = versionInfo,
            canReceiveMessages = gamePreferences.canReceiveMessages(),
            pictureHash = gamePreferences.pictureHash
        )

    /**
     * Starts friend-game login sequence
     * @param userData Players data
     * @param oppId ID of the opponent you want to play with
     * @see processFriendGameLoginSequence
     */
    private fun startFriendGame(userData: PlayerData, oppId: Int) {
        disposable.add(
            processFriendGameLoginSequence(userData, oppId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when (it) {
                        is ConnectionResult.ConnectedToUser -> play(
                            userData,
                            it.oppData,
                            it.isYouStarter
                        )
                        is ConnectionResult.FriendModeRejected -> {
                            view?.showMessage(R.string.cant_connect_to_friend)
                            onCancel()
                        }
                    }
                }, {
                    onCancel()
                    view?.handleError(it)
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
        view?.checkForWaiting {
            disposable.add(
                processLoginSequence(userData, friendsInfo)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        when (it) {
                            is ConnectionResult.ConnectedToUser -> play(
                                userData,
                                it.oppData,
                                it.isYouStarter
                            )
                            is ConnectionResult.FriendModeRejected -> {
                                view?.onFriendModeResult(it.reason, it.login)
                                onCancel()
                            }
                        }
                    }, {
                        onCancel()
                        view?.handleError(it)
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
    ): Observable<ConnectionResult> {
        return networkRepository.login(userData)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { view?.updateInfo(R.string.waiting_for_friend) }
            .observeOn(Schedulers.io())
            .flatMap { networkRepository.acceptFriendRequest(oppId) }
            .flatMapMaybe { networkRepository.connectToFriend() }
    }

    /**
     * This function creates game login sequence.
     * @param userData Players data
     * @param friendsInfo for random pair mode should contain info about friend you want to invite
     * @return Observable with result of login sequence. If something went wrong will return error.
     */
    private fun processLoginSequence(
        userData: PlayerData,
        friendsInfo: FriendInfo?
    ): Observable<ConnectionResult> {
        return networkRepository
            .login(userData)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                view!!.updateInfo(R.string.connecting_to_server)
            }
            .doOnNext {
                view?.let { view ->
                    view.updateInfo(R.string.waiting_for_opp)
                    view.updatePictureHash(userData.authData.credentials.userId, it.picHash)
                    if (it.newerBuild > userData.versionInfo.versionCode)
                        view.showMessage(R.string.new_version_available)
                }
            }
            .observeOn(Schedulers.io())
            .flatMap {
                networkRepository.play(friendsInfo?.userId)
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
            NetworkUser(networkServer, oppData, picasso),
            Player(networkServer, playerData, picasso)
        ).apply {
            if (youStarter)
                reverse()
        }

        view?.onStartGame(GameSession(users, networkServer, GameMode.MODE_NET))
    }

}