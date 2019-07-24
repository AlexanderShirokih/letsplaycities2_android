package ru.aleshi.letsplaycities.network

import androidx.lifecycle.Observer
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo

interface NetworkContract {

    interface View {
        fun getGamePreferences(): GamePreferences
        fun setupWithSN()
        fun setup()
        fun checkForWaiting(task: () -> Unit)
        fun notifyAboutUpdates()
        fun showMessage(msgResId: Int)
        fun handleError(throwable: Throwable)
        fun onCancel()
        fun onResult(requestCode: Int, key: String, value: String)
        fun onStartGame(session: GameSession)
        fun updateInfo(infoMsgId: Int)
    }

    interface Presenter {
        fun onAttachView(view: View)
        fun onLogout()
        fun onConnect(versionInfo: Pair<String, Int>)
        fun onCancel()
        fun onFriendsInfo(versionInfo: Pair<String, Int>): Observer<in FriendsInfo>
        fun onLogin(): Observer<in String>
        fun onDispose()
    }
}