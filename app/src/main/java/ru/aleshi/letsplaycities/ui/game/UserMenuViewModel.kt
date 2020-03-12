package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.utils.Event

/**
 * ViewModel for communication between [GameFragment] and [UserContextMenuDialog]
 */
class UserMenuViewModel : ViewModel() {

    enum class Action { BanUser, SendFriendRequest }

    class UserMenuAction(val userId: Int, val login: String, val action: Action)

    val actions = MutableLiveData<Event<UserMenuAction>>()

    /**
     * Sends friend request to [userId].
     * @param userId id of user that we want to add to friends
     */
    fun sendFriendRequest(userId: Int) {
        actions.postValue(Event(UserMenuAction(userId, "", Action.SendFriendRequest)))
    }

    /**
     * Sends ban message to [userId]. After successful sending this message game will be stopped
     * and pop back parent view.
     */
    fun banUser(userId: Int, login: String) {
        actions.postValue(Event((UserMenuAction(userId, login, Action.BanUser))))
    }
}