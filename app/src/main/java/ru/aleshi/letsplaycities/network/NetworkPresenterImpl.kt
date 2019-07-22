package ru.aleshi.letsplaycities.network

import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.AuthData
import ru.aleshi.letsplaycities.base.player.NetworkUser
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.PlayerData
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo
import ru.aleshi.letsplaycities.network.lpsv3.LPSException
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient
import ru.aleshi.letsplaycities.network.lpsv3.NetworkRepository
import ru.aleshi.letsplaycities.social.NativeAccess
import ru.aleshi.letsplaycities.social.SocialNetworkLoginListener
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.aleshi.letsplaycities.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.File


class NetworkPresenterImpl : NetworkContract.Presenter {

    private val mDisposable: CompositeDisposable = CompositeDisposable()
    private val mNetworkRepository: NetworkRepository = NetworkRepository(NetworkClient())
    private var mView: NetworkContract.View? = null
    private lateinit var mAuthData: AuthData

    override fun onAttachView(view: NetworkContract.View) {
        mView = view
        val prefs = view.getGamePreferences()
        if (prefs.isLoggedFromAnySN()) {
            mAuthData = AuthData.loadFromPreferences(prefs)
            view.setupWithSN()
        }

        SocialNetworkManager.registerCallback(object : SocialNetworkLoginListener {

            override fun onLoggedIn(data: AuthData) {
                mView?.let {
                    mAuthData = data.apply { saveToPreferences(it.getGamePreferences()) }
                    it.setupWithSN()
                }
            }

            override fun onError() {
                mView?.showMessage(R.string.auth_error)
            }

        })
    }

    override fun onLogout() {
        mView?.run {
            SocialNetworkManager.logout(getGamePreferences())
            setup()
        }
    }

    override fun onConnect(versionInfo: Pair<String, Int>) {
        createPlayerData(versionInfo) {
            startGame(it, null)
        }
    }

    override fun onFriendsInfo(versionInfo: Pair<String, Int>): Observer<in FriendsInfo> {
        return Observer { friendsInfo ->
            friendsInfo?.run {
                createPlayerData(versionInfo) { playerData ->
                    startGame(playerData, friendsInfo)
                }
            }
        }
    }

    override fun onLogin(): Observer<in String> {
        return Observer { mView?.onResult(NativeAccess.REQUEST_NATIVE_ACCESS, "login", it!!) }
    }

    override fun onCancel() {
        mNetworkRepository.disconnect()
        mView?.onCancel()
    }

    private fun createPlayerData(versionInfo: Pair<String, Int>, callback: (playerData: PlayerData) -> Unit) {
        mView?.let {
            val prefs = it.getGamePreferences()
            val userData = PlayerData.create(mAuthData.login).apply {
                setBuildInfo(versionInfo.first, versionInfo.second)
                authData = mAuthData
                canReceiveMessages = prefs.canReceiveMessages()
            }

            val path = prefs.getAvatarPath()
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    Utils.loadAvatar(file.toUri())
                        .doOnNext { bitmap ->
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                            userData.avatar = stream.toByteArray()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext {
                            callback(userData)
                        }
                        .subscribe()
                } else
                    callback(userData)
            } else callback(userData)
        }
    }


    private fun startGame(userData: PlayerData, friendsInfo: FriendsInfo?) {
        mView?.checkForWaiting {
            mDisposable.add(mNetworkRepository.login(userData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    mView?.let { view ->
                        it.authData.saveToPreferences(view.getGamePreferences())
                        if (it.newerBuild > userData.clientBuild)
                            view.notifyAboutUpdates()
                    }
                }
                .observeOn(Schedulers.io())
                .flatMap { mNetworkRepository.play(friendsInfo != null, friendsInfo?.userId) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    mView?.run {
                        if (getBanManager().checkInBanList(it.first.authData!!)) {
                            mView?.showMessage(R.string.banned_player)
                            mNetworkRepository.disconnect()
                            throw LPSException("banned")
                        }
                    }
                }
                .retry { t -> "banned" == t.message }
                .subscribe({ play(userData, it.first, it.second) }) {
                    onCancel()
                    mView?.handleError(it)
                })
        }
    }

    private fun play(playerData: PlayerData, oppData: PlayerData, youStarter: Boolean) {
        val users = arrayOf(
            NetworkUser(oppData),
            Player(playerData)
        ).apply {
            if (youStarter)
                reverse()
        }

        mView?.onStartGame(GameSession(users, NetworkServer(mNetworkRepository)))
    }

}