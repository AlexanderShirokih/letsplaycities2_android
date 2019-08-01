package ru.aleshi.letsplaycities.network

import android.annotation.SuppressLint
import ru.aleshi.letsplaycities.base.player.PlayerData
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient
import ru.aleshi.letsplaycities.network.lpsv3.NetworkRepository

class FriendRequestPresenter(private val mView: FriendRequestContract.View) : FriendRequestContract.Presenter {

    @SuppressLint("CheckResult")
    override fun onDecline(userId: Int) {
        PlayerData.load(mView.gamePreferences())?.let { userData ->
            NetworkRepository(NetworkClient()).apply {
                login(userData)
                    .flatMapCompletable { sendFriendRequestResult(false, userId) }
                    .subscribe({}, { e -> mView.onError(e) })
            }
        }
    }

}