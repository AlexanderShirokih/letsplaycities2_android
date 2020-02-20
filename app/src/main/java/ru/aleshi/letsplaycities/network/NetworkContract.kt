package ru.aleshi.letsplaycities.network

import androidx.lifecycle.Observer
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.ui.profile.ProfileViewModel
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.FriendModeResult

interface NetworkContract {

    interface View {
        fun getProfileViewModel(): ProfileViewModel
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
        fun onConnectToFriendGame(oppId: Int)
        fun onConnect()
        fun onCancel()
        fun onFriendsInfo(): Observer<in FriendInfo>
        fun onDispose()
    }
}