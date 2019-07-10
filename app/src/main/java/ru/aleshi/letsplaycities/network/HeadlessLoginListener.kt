package ru.aleshi.letsplaycities.network

import ru.aleshi.letsplaycities.network.lpsv3.ILogInListener
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient


abstract class HeadlessLoginListener : ILogInListener {

    override fun onLoggedIn(data: AuthData) {
        onSuccess(data)
    }

    override fun onPlay(data: PlayerData, youStarter: Boolean) {

    }

    override fun onLoginFailed(banReason: String?, connError: String) {
        onError(banReason ?: connError)
    }

    override fun onNewerBuildAvailable() {

    }

    override fun onKicked(isSystem: Boolean, desc: String) {
        onError(desc)
    }

    override fun onFriendModeRequest(result: FriendModeResult, login: String?, userId: Int) {

    }

    override fun onConnect(nc: NetworkClient, userData: PlayerData, state: NetworkClient.PlayState) {

    }

    override fun onRequestFirebaseToken() {
        NetworkUtils.updateToken()
    }

    abstract fun onSuccess(ad: AuthData)

    abstract fun onError(msg: String)
}
