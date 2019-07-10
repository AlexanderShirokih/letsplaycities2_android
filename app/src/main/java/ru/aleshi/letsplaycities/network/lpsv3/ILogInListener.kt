package ru.aleshi.letsplaycities.network.lpsv3

import ru.aleshi.letsplaycities.network.FriendModeResult
import ru.aleshi.letsplaycities.network.PlayerData
import ru.aleshi.letsplaycities.social.AuthData


interface ILogInListener {
    fun onLoggedIn(data: AuthData)

    fun onPlay(data: PlayerData, youStarter: Boolean)

    fun onLoginFailed(banReason: String?, connError: String)

    fun onNewerBuildAvailable()

    fun onKicked(isSystem: Boolean, desc: String)

    fun onFriendModeRequest(result: FriendModeResult, login: String?, userId: Int)

    fun onConnect(nc: NetworkClient, userData: PlayerData, state: NetworkClient.PlayState)

    fun onRequestFirebaseToken()
}