package ru.aleshi.letsplaycities.network

import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.ui.profile.ProfileViewModel
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.FriendModeResult

interface NetworkContract {

    interface View {
        fun getProfileViewModel(): ProfileViewModel
        fun setupLayout(isLoggedIn: Boolean, isLocal: Boolean)
        fun checkForWaiting(task: () -> Unit)
        fun showMessage(msgResId: Int)
        fun handleError(throwable: Throwable)
        fun onCancel()
        fun onStartGame(session: GameSession)
        fun updateInfo(infoMsgId: Int)
        fun onFriendModeResult(result: FriendModeResult, login: String?)
    }

    interface Presenter {
        fun onAttachView(view: View, isLocal: Boolean)
        fun onConnectToFriendGame(oppId: Int)
        fun onConnect(friendsInfo: FriendInfo? = null)
        fun onCancel()
        fun onDispose()
    }
}