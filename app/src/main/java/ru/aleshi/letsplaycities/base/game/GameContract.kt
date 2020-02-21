package ru.aleshi.letsplaycities.base.game

import android.content.Context
import com.squareup.picasso.RequestCreator
import io.reactivex.Completable
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.dictionary.Dictionary
import ru.aleshi.letsplaycities.base.dictionary.DictionaryUpdater

interface GameContract {

    interface View {
        fun showUserMenu(isFriend: Boolean, name: String, userId: Int)
        fun showInfo(msg: String)
        fun showError(err: Throwable)
        fun updateLabel(info: String, position: Position)
        fun updateAvatar(imageRequest: RequestCreator, position: Position)
        fun context(): Context
        fun putMessage(message: String, position: Position)
        fun putCity(city: String, countryCode: Short, position: Position)
        fun updateCity(city: String, hasErrors: Boolean)
        fun showGameResults(result: String, score: Int)
        fun showCorrectionDialog(word: String, errorMsg: String)
        fun getGamePreferences(): GamePreferences
        fun onTimerUpdate(time: String)
        fun onHighlightUser(position: Position)
        fun setMenuItemsVisibility(help: Boolean, msg: Boolean)
        fun downloadingListener(): DictionaryUpdater.DownloadingListener
        fun showFriendRequestDialog(name: String)
        fun comboSystemView(): ComboSystemView
    }

    interface Presenter {
        fun onAttachView(view: View)
        fun onDetachView()
        fun onStop()
        fun submit(userInput: String, callback: () -> Unit): Boolean
        fun useHint()
        fun onSurrender()
        fun postCorrectedWord(word: String?, errorMsg: String?)
        fun sendMessage(message: String) : Completable
        fun correct(word: String, errorMsg: String)
        fun dictionary(): Dictionary
        fun needsShowMenu(position: Position)
        fun sendFriendRequest() : Completable
        fun onFriendRequestResult(isAccepted: Boolean) : Completable
        fun banUser(userId: Int) : Completable
    }

}