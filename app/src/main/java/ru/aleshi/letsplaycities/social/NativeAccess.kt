package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import ru.aleshi.letsplaycities.base.player.AuthData
import ru.aleshi.letsplaycities.base.player.DeviceId

class NativeAccess : ISocialNetwork() {
    companion object {
        const val REQUEST_NATIVE_ACCESS = 4312
    }

    private var userLogin: String = "User"

    override fun onInitialize(context: Context) {}

    override fun onLogin(activity: Activity) {
        onLoggedIn(activity, "native_access")
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {
        val info = AuthData(userLogin, DeviceId().toString(), "nv", access_token)
        callback?.onLoggedIn(info)
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_NATIVE_ACCESS)
            return false

        userLogin = data!!.extras!!.getString("login")!!
        SocialNetworkManager.login(ServiceType.NV, activity)
        return true
    }
}