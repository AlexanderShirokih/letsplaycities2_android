package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.Result
import ru.aleshi.letsplaycities.ui.BaseAsyncActivity
import ru.quandastudio.lpsclient.model.AuthType

class Google : ISocialNetwork() {

    override fun onInitialize(context: Context) = Unit

    override suspend fun onLogin(activity: Activity) {
        GoogleAccountHelper.signIn(activity as BaseAsyncActivity).apply {
            if (this is Result.Success) {
                val account = value

                val isAllRequestedDataPresent =
                    account.id != null && account.serverAuthCode != null && account.displayName != null && account.photoUrl != null

                if (isAllRequestedDataPresent)
                    callback?.onLoggedIn(
                        SocialAccountData(
                            snUID = account.id!!,
                            login = account.displayName!!,
                            accessToken = account.serverAuthCode!!,
                            networkType = AuthType.Google,
                            pictureUri = account.photoUrl!!
                        )
                    )
                else {
                    callback?.onError(activity.getString(R.string.not_all_requested_data_received))
                    GoogleAccountHelper.signOut(activity)
                }
            } else if (this is Result.Failure) {
                callback?.onError(error.message ?: "")
            }
        }
    }


    override fun onLoggedIn(activity: Activity, accessToken: String) {
    }

    override suspend fun onLogout(activity: Activity) {
        super.onLogout(activity)
        GoogleAccountHelper.signOut(activity)
    }

    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) = false
}
