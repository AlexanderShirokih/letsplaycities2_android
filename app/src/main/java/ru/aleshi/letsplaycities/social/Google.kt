package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import ru.quandastudio.lpsclient.model.AuthType

class Google : ISocialNetwork() {
    companion object {
        const val RC_SIGN_IN = 2635
    }

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private var mGoogleSignInAccount: GoogleSignInAccount? = null

    private var fromSilent = false

    override fun onInitialize(context: Context) {
        //725939428380-erg4fr1c9ba7qqmhleh3ktjnck9gt5du.apps.googleusercontent.com
        mGoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestProfile()
                .requestIdToken("725939428380-a1370ah3l6bjcio0hg2jgt8kvb9kuhmj.apps.googleusercontent.com")
                .build()

    }

    override fun onLogin(activity: Activity) {
        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(activity)
        if (mGoogleSignInAccount != null) {
            onLoggedIn(mGoogleSignInAccount!!)
        } else {
            mGoogleSignInClient = GoogleSignIn.getClient(activity, mGoogleSignInOptions)
            // Build a GoogleSignInClient with the options specified by gso.
            val signInIntent = mGoogleSignInClient.signInIntent
            activity.startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    //TODO: Test w\o Game.Scope
    //TODO: Add SignIn button to layout
    fun signIn(activity: Activity) {
        initialize(activity)
        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(activity)
        if (!GoogleSignIn.hasPermissions(mGoogleSignInAccount, *mGoogleSignInOptions.scopeArray)) {
            mGoogleSignInClient.silentSignIn().addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    mGoogleSignInAccount = task.result
                    Log.d("TAG", "Logged In from SILENT")
                } else {
                    //TODO: Test
                    Log.d("TAG", "Logged from silent")
                    fromSilent = true
                    val signInIntent = mGoogleSignInClient.signInIntent
                    activity.startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            }
        }
    }

    private fun onLoggedIn(account: GoogleSignInAccount) {
        callback?.onLoggedIn(
            SocialAccountData(
                snUID = account.id!!,
                login = account.displayName!!,
                accessToken = account.idToken!!,
                networkType = AuthType.Google,
                pictureUri = account.photoUrl!!
            )
        )
    }

    override fun onLoggedIn(activity: Activity, accessToken: String) {

    }

    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        if (requestCode != RC_SIGN_IN)
            return false
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            Log.d("TAG", "isSucc={${task.isSuccessful}, taskEX=${task.exception}")
            if (task.isSuccessful) {
                mGoogleSignInAccount = task.getResult(ApiException::class.java)
                // Signed in successfully, show authenticated UI.
                if (!fromSilent)
                    onLoggedIn(mGoogleSignInAccount!!)
            } else {
                Log.d(
                    "TAG",
                    "ex=${GoogleSignInStatusCodes.getStatusCodeString((task.exception as ApiException).statusCode)}"
                )
                task.exception?.printStackTrace()
                onError()
            }
        } catch (e: ApiException) {
            onError()
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GoogleSignIN", "signInResult:failed code=" + e.statusCode)
        }
        fromSilent = false
        return true
    }
}
