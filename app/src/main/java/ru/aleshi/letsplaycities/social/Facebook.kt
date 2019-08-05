package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import ru.quandastudio.lpsclient.model.AuthData

class Facebook : ISocialNetwork() {

    private var callbackManager: CallbackManager? = null

    override fun onInitialize(context: Context) {
        callbackManager = CallbackManager.Factory.create()
    }

    override fun onLogin(activity: Activity) {
        val loginManager = LoginManager.getInstance()

        object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile) {
                stopTracking()

                val accessToken = AccessToken.getCurrentAccessToken()
                if (accessToken != null && !accessToken.isExpired) {
                    val login = currentProfile.name
                    val userID = currentProfile.id

                    val picture = currentProfile.getProfilePictureUri(128, 128)

                    SocialUtils.saveAvatar(activity, picture) {
                        val info = AuthData(login, userID, "fb", accessToken.token)
                        callback?.onLoggedIn(info)
                    }
                }
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

    override fun onLoggedIn(activity: Activity, access_token: String) {

    }


    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

}