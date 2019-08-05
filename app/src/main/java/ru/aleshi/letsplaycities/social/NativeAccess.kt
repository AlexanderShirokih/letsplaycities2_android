package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import ru.aleshi.letsplaycities.base.player.DeviceId
import ru.quandastudio.lpsclient.model.AuthData

class NativeAccess : ISocialNetwork() {

    internal var userLogin: String = "User"

    override fun onInitialize(context: Context) {}

    override fun onLogin(activity: Activity) {
        onLoggedIn(activity, "native_access")
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {
        val info = AuthData(userLogin, DeviceId(activity).toString(), "nv", access_token)
        callback?.onLoggedIn(info)
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean = false

    companion object {
        fun login(login: String, activity: Activity) {
            (ServiceType.NV.network as NativeAccess).userLogin = login
            SocialNetworkManager.login(ServiceType.NV, activity)
        }
    }
}