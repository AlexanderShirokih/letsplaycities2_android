package ru.aleshi.letsplaycities.base.game

import android.content.Context
import android.graphics.drawable.Drawable
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.ui.game.DictionaryUpdater

interface GameContract {


    interface View {
        fun showUserMenu(isFriend:Boolean, name: String, userId: Int)
        fun showInfo(msg: String)
        fun showError(err: Throwable)
        fun updateLabel(info: String, left: Boolean)
        fun updateAvatar(image: Drawable, left: Boolean)
        fun context(): Context
        fun putMessage(message: String, isLeft: Boolean)
        fun putCity(city: String, countryCode: Short, left: Boolean)
        fun updateCity(city: String, hasErrors: Boolean)
        fun showGameResults(result: String, score: Int)
        fun showCorrectionDialog(word: String, errorMsg: String)
        fun getGamePreferences() : GamePreferences
        fun onTimerUpdate(time: String)
        fun onHighlightUser(left: Boolean)
        fun setMenuItemsVisibility(help: Boolean, msg: Boolean)
        fun downloadingListener(): DictionaryUpdater.DownloadingListener
        fun showFriendRequestDialog(name: String)
    }

    interface Presenter {
        fun onAttachView(view: View)
        fun onDetachView()
        fun onStop()
        fun submit(userInput: String, callback: () -> Unit): Boolean
        fun useHint()
        fun onSurrender()
        fun postCorrectedWord(word: String?, errorMsg: String?)
        fun sendMessage(message: String)
        fun correct(word: String, errorMsg: String)
        fun dictionary() : Dictionary
        fun needsShowMenu(isLeft: Boolean)
        fun sendFriendRequest()
        fun onFriendRequestResult(isAccepted: Boolean)
        fun banUser(userId: Int)
    }

}