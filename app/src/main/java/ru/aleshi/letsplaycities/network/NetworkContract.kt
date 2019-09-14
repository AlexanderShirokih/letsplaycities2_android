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
        fun onConnectToFriendGame(versionInfo: Pair<String, Int>, oppId: Int)
        fun onConnect(versionInfo: Pair<String, Int>)
        fun onCancel()
        fun onFriendsInfo(versionInfo: Pair<String, Int>): Observer<in FriendInfo>
        fun onDispose()
    }
}