package ru.aleshi.letsplaycities.network

import android.annotation.SuppressLint
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.base.player.GamePlayerDataFactory
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.core.NetworkClient

class FriendRequestPresenter(private val mView: FriendRequestContract.View) :
    FriendRequestContract.Presenter {

    @SuppressLint("CheckResult")
    override fun onDecline(userId: Int) {
        //TODO: Inject variables
        GamePlayerDataFactory(GameAuthDataFactory())
            .load(mView.gamePreferences())?.let { userData ->
                NetworkRepository(
                    NetworkClient(AndroidBase64Provider, false, NetworkClient.ConnectionType.WebSocket, BuildConfig.HOST),
                    NetworkUtils.getToken()
                ).apply {
                    login(userData)
                        .flatMap { sendFriendRequestResult(false, userId) }
                        .subscribe({}, { e -> mView.onError(e) })
                }
            }
    }

}