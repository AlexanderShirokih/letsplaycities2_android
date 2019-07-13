package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import ru.aleshi.letsplaycities.base.AuthData
import ru.aleshi.letsplaycities.base.DeviceId

class NativeAccess : ISocialNetwork() {
    var userLogin: String = "User"

    override fun onInitialize(context: Context) {}

    override fun onLogin(activity: Activity) {
        onLoggedIn(activity, "native_access")
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {
        val info = AuthData(userLogin, DeviceId().toString(), "nv", access_token)
        callback?.onLoggedIn(info)
    }

}