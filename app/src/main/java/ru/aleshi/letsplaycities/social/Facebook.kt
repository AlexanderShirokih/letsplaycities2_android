package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import ru.quandastudio.lpsclient.model.AuthType

class Facebook : ISocialNetwork() {

    private var callbackManager: CallbackManager? = null

    override fun onInitialize(context: Context) {
        callbackManager = CallbackManager.Factory.create()
    }

    override suspend fun onLogin(activity: Activity) {
        val loginManager = LoginManager.getInstance()

        object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile) {
                stopTracking()
            }
        }

        loginManager.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    onLoggedIn(activity, loginResult.accessToken.token)
                }

                override fun onCancel() {
                }

                override fun onError(exception: FacebookException) {
                    exception.printStackTrace()
                    this@Facebook.onError()
                }
            })
        loginManager.logInWithReadPermissions(activity, listOf("public_profile"))
    }

    override fun onLoggedIn(activity: Activity, accessToken: String) {
        val currentProfile = Profile.getCurrentProfile()
        if (currentProfile == null)
            callback?.onError("Facebook profile is NULL")
        else
            callback?.onLoggedIn(
                SocialAccountData(
                    snUID = currentProfile.id,
                    login = currentProfile.name,
                    accessToken = accessToken,
                    networkType = AuthType.Facebook,
                    pictureUri = currentProfile.getProfilePictureUri(128, 128)
                )
            )
    }


    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        return callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

}