package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.AuthType


class Google : ISocialNetwork() {
    companion object {
        const val RC_SIGN_IN = 2635
    }

    private lateinit var gso: GoogleSignInOptions


    override fun onInitialize(context: Context) {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("725939428380-a1370ah3l6bjcio0hg2jgt8kvb9kuhmj.apps.googleusercontent.com")
            .build()
    }

    override fun onLogin(activity: Activity) {

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(activity)
        if (account != null) {
            onLoggedIn(activity, account)
        } else {
            // Build a GoogleSignInClient with the options specified by gso.
            val mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)

            val signInIntent = mGoogleSignInClient.signInIntent
            activity.startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun onLoggedIn(activity: Activity, account: GoogleSignInAccount) {
        val accessToken = account.idToken!!
        val login = account.displayName
        val uid = account.id
        val picture = account.photoUrl

        SocialUtils.saveAvatar(activity, picture!!) {
            val info = AuthData(login!!, uid!!, AuthType.Google, accessToken)
            callback?.onLoggedIn(info)
        }
    }

    override fun onLoggedIn(activity: Activity, access_token: String) {

    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != RC_SIGN_IN)
            return false
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            onLoggedIn(activity, account!!)
        } catch (e: ApiException) {
            onError()
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GoogleSignIN", "signInResult:failed code=" + e.statusCode)
        }
        return true
    }
}
