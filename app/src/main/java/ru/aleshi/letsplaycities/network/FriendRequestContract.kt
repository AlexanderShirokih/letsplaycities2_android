package ru.aleshi.letsplaycities.network

import ru.aleshi.letsplaycities.base.GamePreferences

interface FriendRequestContract {

    interface Presenter {

        fun onDecline(userId: Int)
    }

    interface View {
        fun onError(exception: Throwable)
        fun gamePreferences(): GamePreferences
    }
}