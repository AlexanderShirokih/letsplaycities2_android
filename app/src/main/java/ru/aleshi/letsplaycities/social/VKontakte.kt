package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.requests.VKRequest
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.quandastudio.lpsclient.model.AuthData

class VKontakte : ISocialNetwork() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    internal class VKUser(json: JSONObject) {
        val id = json.optInt("id", 0)
        val login = json.optString("first_name", "") + " " + json.optString("last_name", "")
        val photo = json.optString("photo_100", "").toUri()
    }

    internal class VKUsersRequest : VKRequest<VKUser>("users.get") {
        init {
            addParam("fields", "photo_100")
        }

        override fun parse(r: JSONObject): VKUser {
            val users = r.getJSONArray("response")
            return VKUser(users.getJSONObject(0))
        }
    }

    override fun onInitialize(context: Context) {
//        VKSdk.initialize(context)
    }

    override fun onLogin(activity: Activity) {
        VK.login(activity)
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {
        disposable.add(Observable.fromCallable { VK.executeSync(VKUsersRequest()) }
            .subscribeOn(Schedulers.single())
            .flatMap {
                Observable.zip(
                    Observable.just(it),
                    SocialUtils.updateAvatar(activity as MainActivity, it.photo),
                    BiFunction<VKUser, String, AuthData> { user, _ ->
                        AuthData(user.login, user.id.toString(), "vk", access_token)
                    }
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ callback?.onLoggedIn(it) }, { callback?.onError() }))
    }

    override fun onLogout() {
        super.onLogout()
        disposable.clear()
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return VK.onActivityResult(requestCode, resultCode, data, object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                onLoggedIn(activity, token.accessToken)
            }

            override fun onLoginFailed(errorCode: Int) = onError()
        })
    }
}