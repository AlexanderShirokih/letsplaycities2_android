package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.ok.android.sdk.ContextOkListener
import ru.ok.android.sdk.Odnoklassniki
import ru.ok.android.sdk.OkRequestMode
import ru.ok.android.sdk.util.OkAuthType
import ru.ok.android.sdk.util.OkScope
import ru.quandastudio.lpsclient.model.AuthData


@ExperimentalCoroutinesApi
class OdnoklassnikiSN : ISocialNetwork() {

    private val disposable: CompositeDisposable = CompositeDisposable()
    private lateinit var odnoklassniki: Odnoklassniki

    companion object {
        private const val REDIRECT_URI = "okauth://ok1267998976"
    }

    override fun onInitialize(context: Context) {
        odnoklassniki = Odnoklassniki.createInstance(context, "1267998976", "CBACCFJMEBABABABA")
    }

    override fun onLogin(activity: Activity) {
        odnoklassniki.requestAuthorization(
            activity,
            REDIRECT_URI,
            OkAuthType.ANY,
            OkScope.VALUABLE_ACCESS,
            OkScope.LONG_ACCESS_TOKEN
        )
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {
        disposable.add(Observable.fromCallable {
            odnoklassniki.request(
                "users.getCurrentUser",
                null,
                OkRequestMode.DEFAULT
            )!!
        }
            .subscribeOn(Schedulers.single())
            .filter { it.isNotEmpty() }
            .map { JSONObject(it) }
            .flatMap {
                Observable.zip(
                    Observable.just(it),
                    SocialUtils.updateAvatar(activity as MainActivity, it.getString("pic_3").toUri()),
                    BiFunction<JSONObject, String, AuthData> { user, _ ->
                        AuthData(user.getString("name"), user.getString("uid"), "ok", access_token)
                    }
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ callback?.onLoggedIn(it) }, { it.printStackTrace(); callback?.onError() })
        )
    }

    override fun onLogout() {
        odnoklassniki.clearTokens()
        disposable.clear()
        super.onLogout()
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (odnoklassniki.isActivityRequestOAuth(requestCode)) {
            // process OAUTH sign-in response
            odnoklassniki.onAuthActivityResult(requestCode, resultCode, data, ContextOkListener(activity,
                onSuccess = { _, json -> onLoggedIn(activity, json.getString("access_token")) },
                onError = { _, _ -> onError() },
                onCancel = { _, _ -> Unit }
            ))
        }
        return false
    }

}