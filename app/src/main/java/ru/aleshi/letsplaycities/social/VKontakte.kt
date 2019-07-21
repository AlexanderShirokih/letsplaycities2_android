package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.*
import com.vk.sdk.api.model.VKApiUser
import com.vk.sdk.api.model.VKList
import ru.aleshi.letsplaycities.base.player.AuthData


class VKontakte : ISocialNetwork() {

    override fun onInitialize(context: Context) {
        VKSdk.initialize(context)
    }

    override fun onLogin(activity: Activity) {
        VKSdk.login(activity)
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {
        val request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_100"))
        request.secure = false
        request.executeWithListener(object : VKRequest.VKRequestListener() {
            override fun onComplete(response: VKResponse?) {
                val user = (response!!.parsedModel as VKList<VKApiUser>)[0]
                val login = user.first_name + " " + user.last_name

                SocialUtils.saveAvatar(activity, user.photo_100.toUri()) {
                    val info =
                        AuthData(login, user.id.toString(), "vk", access_token)
                    callback?.onLoggedIn(info)
                }
            }
        })
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(res: VKAccessToken) {
                onLoggedIn(activity, res.accessToken)
            }

            override fun onError(error: VKError) {
                onError()
            }

        })
    }
}