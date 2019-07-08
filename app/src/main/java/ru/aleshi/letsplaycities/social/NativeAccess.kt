package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import ru.aleshi.letsplaycities.base.DeviceId

class NativeAccess : ISocialNetwork() {
    var userLogin: String = "Player"

    override fun onInitialize(context: Context) {}

    override fun onLogin(activity: Activity) {
        onLoggedIn(activity, "native_access")
    }

    override fun onLoggedIn(context: Context, access_token: String) {
        val info = SocialInfo(userLogin, DeviceId().toString(), "nv")
        callback?.onLoggedIn(info, access_token)
    }

}