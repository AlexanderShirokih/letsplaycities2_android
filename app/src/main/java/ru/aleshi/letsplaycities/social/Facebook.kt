package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.Profile.getCurrentProfile
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult


class Facebook : ISocialNetwork() {

    var callbackManager: CallbackManager? = null

    override fun onInitialize(context: Context) {
        callbackManager = CallbackManager.Factory.create()
    }

    override fun onLogin(activity: Activity) {
        val loginManager = LoginManager.getInstance()

        object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile, currentProfile: Profile) {
                stopTracking()
            }
        }

        loginManager.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    onLoggedIn(activity, loginResult.accessToken.token)
                }

                override fun onCancel() {
                    this@Facebook.onError()
                }

                override fun onError(exception: FacebookException) {
                    this@Facebook.onError()
                }
            })
        loginManager.logInWithReadPermissions(activity, listOf("public_profile"))
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {
        val accessToken = AccessToken.getCurrentAccessToken()
        val profile = getCurrentProfile()
        if (accessToken != null && !accessToken.isExpired && profile != null) {
            val login = profile.name
            val userID = profile.id

            val picture = profile.getProfilePictureUri(128, 128)

            SocialUtils.saveAvatar(activity, picture) {
                val info = SocialInfo(login, userID, "fb")
                callback?.onLoggedIn(info, access_token)
            }
        }
    }

}