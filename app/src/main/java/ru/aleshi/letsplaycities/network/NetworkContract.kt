package ru.aleshi.letsplaycities.network

import androidx.lifecycle.Observer
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.quandastudio.lpsclient.model.FriendModeResult
import ru.quandastudio.lpsclient.model.FriendInfo

interface NetworkContract {

    interface View {
        fun getGamePreferences(): GamePreferences
        fun setupLayout(isLoggedIn: Boolean)
        fun checkForWaiting(task: () -> Unit)
        fun notifyAboutUpdates()
        fun showMessage(msgResId: Int)
        fun handleError(throwable: Throwable)
        fun onCancel()
        fun onStartGame(session: GameSession)
        fun updateInfo(infoMsgId: Int)
        fun onFriendModeResult(result: FriendModeResult, login: String?)
    }

    interface Presenter {
        fun onAttachView(view: View)
        fun onConnectToFriendGame(versionInfo: VersionInfo, oppId: Int)
        fun onConnect(versionInfo: VersionInfo)
        fun onCancel()
        fun onFriendsInfo(versionInfo: VersionInfo): Observer<in FriendInfo>
        fun onDispose()
    }
}