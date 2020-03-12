package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.platform.AndroidDeviceId
import ru.quandastudio.lpsclient.model.AuthType

class NativeAccess : ISocialNetwork() {

    override fun onInitialize(context: Context) {}

    override fun onLogin(activity: Activity) {
        onLoggedIn(activity, "native_access")
    }

    override fun onLoggedIn(activity: Activity, accessToken: String) {
        val prefs = (activity.application as LPSApplication).gamePreferences
        callback?.onLoggedIn(
            SocialAccountData(
                snUID = AndroidDeviceId(activity).toString(),
                login = prefs.lastNativeLogin,
                accessToken = accessToken,
                networkType = AuthType.Native,
                pictureUri = prefs.lastAvatarUri?.toString()?.toUri() ?: Uri.EMPTY
            )
        )
    }

    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean = false

}