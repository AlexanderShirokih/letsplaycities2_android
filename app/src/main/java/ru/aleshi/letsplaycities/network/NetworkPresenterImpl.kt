package ru.aleshi.letsplaycities.network

import androidx.lifecycle.Observer
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
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo
import javax.inject.Inject

/**
 * This class handles onConnect events and initiates login process
 */
class NetworkPresenterImpl @Inject constructor(
    @AppVersionInfo
    private val versionInfo: VersionInfo,
    private val mNetworkServer: NetworkServer,
    private val mNetworkRepository: NetworkRepository,
    private val mAuthDataFactory: GameAuthDataFactory,
    private val mPicasso: Picasso,
    private val gamePreferences: GamePreferences
) : NetworkContract.Presenter {

    private lateinit var mAuthData: AuthData
    private val mDisposable: CompositeDisposable = CompositeDisposable()
    private var mView: NetworkContract.View? = null
    private var isLocal = false

    /**
     * Called right after user view was created.
     * Saves view reference and call setupLayout
     * @param view view to be attached
     * @see NetworkContract.View.setupLayout
     */
    override fun onAttachView(view: NetworkContract.View, isLocal: Boolean) {
        this.isLocal = isLocal
        mView = view
        mAuthData = mAuthDataFactory.load()
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
     */
    override fun onConnect() {
        startGame(createPlayerData(), null)
    }

    /**
     * Called when user wants to start game in friend mode.
     */
    override fun onFriendsInfo(): Observer<in FriendInfo> {
        return Observer { friendInfo ->
            friendInfo?.apply {
                startGame(createPlayerData(), friendInfo)
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
        mView?.apply {
            onCancel()
            setupLayout(true, isLocal)
        }
    }

    /**
     * This function is called when user view destroys to dispose
     * resources and interrupt connections.
     */
    override fun onDispose() {
        mDisposable.clear()
    }

    /**
     * Creates new PlayerData/
     */
    private fun createPlayerData() =
        PlayerData(
            authData = mAuthData,
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
            .flatMap { mNetworkRepository.acceptFriendRequest(oppId) }
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
                    view.getProfileViewModel()
                        .updatePictureHash(userData.authData.credentials.userId, it.picHash)
                    if (it.newerBuild > userData.versionInfo.versionCode)
                        view.showMessage(R.string.new_version_available)
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
            NetworkUser(mNetworkServer, oppData, mPicasso),
            Player(mNetworkServer, playerData, mPicasso)
        ).apply {
            if (youStarter)
                reverse()
        }

        mView?.onStartGame(GameSession(users, mNetworkServer, GameMode.MODE_NET))
    }

}