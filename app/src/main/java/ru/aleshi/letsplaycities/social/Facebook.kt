package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import ru.aleshi.letsplaycities.R
import ru.quandastudio.lpsclient.model.AuthType

class Facebook : ISocialNetwork() {

    private var callbackManager: CallbackManager? = null

    override fun onInitialize(context: Context) {
        callbackManager = CallbackManager.Factory.create()
    }

    override suspend fun onLogin(activity: Activity) {
        val loginManager = LoginManager.getInstance()
        object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile?) {
                stopTracking()
            }
        }
        val tokenDeferred = CompletableDeferred<String?>()
        loginManager.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    tokenDeferred.complete(loginResult.accessToken.token)
                }

                override fun onCancel() {
                    tokenDeferred.complete(null)
                }

                override fun onError(exception: FacebookException) {
                    exception.printStackTrace()
                    tokenDeferred.completeExceptionally(exception)
                }
            })
        loginManager.logInWithReadPermissions(activity, listOf("public_profile"))

        try {
            tokenDeferred.await()?.apply {
                for (i in 0..20) {
                    val currentProfile = Profile.getCurrentProfile()
                    if (currentProfile != null) {
                        callback?.onLoggedIn(
                            SocialAccountData(
                                snUID = currentProfile.id,
                                login = currentProfile.name,
                                accessToken = this,
                                networkType = AuthType.Facebook,
                                pictureUri = currentProfile.getProfilePictureUri(128, 128)
                            )
                        )
                        return
                    } else
                        delay(500)
                }
                callback?.onError(activity.getString(R.string.error_fb_null_profile))
            }
        } catch (fEx: FacebookException) {
            callback?.onError(fEx.message)
        }
    }

    override fun onLoggedIn(activity: Activity, accessToken: String) = Unit

    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        return callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    override suspend fun onLogout(activity: Activity) {
        super.onLogout(activity)
        LoginManager.getInstance().logOut()
    }

}