package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import ru.aleshi.letsplaycities.base.player.DeviceId
import ru.quandastudio.lpsclient.model.AuthType

class NativeAccess : ISocialNetwork() {

    //TODO: Remove this
    internal var userLogin: String = "User"

    override fun onInitialize(context: Context) {}

    override fun onLogin(activity: Activity) {
        onLoggedIn(activity, "native_access")
    }

    override fun onLoggedIn(activity: Activity, accessToken: String) {
        callback?.onLoggedIn(
            SocialAccountData(
                snUID = DeviceId(activity).toString(),
                login = userLogin,
                accessToken = accessToken,
                networkType = AuthType.Native,
                pictureUri = Uri.EMPTY
            )
        )
    }

    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean = false

    companion object {
        //TODO: Remove this
        fun login(login: String, activity: Activity) {
            (ServiceType.NV.network as NativeAccess).userLogin = login
            SocialNetworkManager.login(ServiceType.NV, activity)
        }
    }
}