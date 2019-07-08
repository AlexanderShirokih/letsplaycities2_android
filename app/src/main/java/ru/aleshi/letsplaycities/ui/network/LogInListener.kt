package ru.aleshi.letsplaycities.ui.network

import ru.aleshi.letsplaycities.network.*

class LogInListener : ILogInListener {
    override fun onLoggedIn(data: AuthData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlay(data: PlayerData, youStarter: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoginFailed(banReason: String, connError: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNewerBuildAvailable() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onKicked(isSystem: Boolean, desc: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFriendModeRequest(result: FriendModeResult, login: String, userId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnect(nc: NetworkClient, userData: PlayerData, state: NetworkClient.PlayState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRequestFirebaseToken() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}