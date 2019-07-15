package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import ru.aleshi.letsplaycities.base.player.AuthData
import ru.ok.android.sdk.Odnoklassniki
import ru.ok.android.sdk.OkRequestMode
import ru.ok.android.sdk.util.OkAuthType
import ru.ok.android.sdk.util.OkScope
import java.io.IOException


@ExperimentalCoroutinesApi
class OdnoklassnikiSN : ISocialNetwork() {

    companion object {
        private const val REDIRECT_URI = "okauth://ok1267998976"
    }

    override fun onInitialize(context: Context) {
        Odnoklassniki.createInstance(context, "1267998976", "CBACCFJMEBABABABA")
    }

    override fun onLogin(activity: Activity) {
        Odnoklassniki.getInstance().requestAuthorization(
            activity,
            REDIRECT_URI,
            OkAuthType.ANY,
            OkScope.VALUABLE_ACCESS,
            OkScope.LONG_ACCESS_TOKEN
        )
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {
        MainScope().launch {
            val ret = withContext(Dispatchers.IO) {
                try {
                    Odnoklassniki.getInstance().request("users.getCurrentUser", null, OkRequestMode.DEFAULT)
                } catch (e: IOException) {
                    ""
                }
            }

            if (ret.isNotEmpty()) {
                try {
                    val json = JSONObject(ret)

                    SocialUtils.saveAvatar(activity, json.getString("pic_3").toUri()) {
                        val info = AuthData(
                            json.getString("name"),
                            json.getString("uid"),
                            "ok",
                            access_token
                        )
                        callback?.onLoggedIn(info)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onLogout() {
        Odnoklassniki.getInstance().clearTokens()
        super.onLogout()
    }

}