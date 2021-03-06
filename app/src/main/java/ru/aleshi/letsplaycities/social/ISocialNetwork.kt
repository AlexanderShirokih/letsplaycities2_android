package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent

abstract class ISocialNetwork {

    var callback: SocialNetworkLoginListener? = null

    var isInitialized: Boolean = false

    internal fun initialize(context: Context) {
        if (isInitialized)
            return
        onInitialize(context)
        isInitialized = true
    }

    protected abstract fun onInitialize(context: Context)

    suspend fun login(activity: Activity) {
        onLogin(activity)
    }

    protected abstract suspend fun onLogin(activity: Activity)

    abstract fun onLoggedIn(activity: Activity, accessToken: String)

    open suspend fun onLogout(activity: Activity) {
        isInitialized = false
    }

    protected abstract fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean

    fun sendResult(
        mainActivity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        return if (isInitialized) onActivityResult(
            mainActivity,
            requestCode,
            resultCode,
            data
        ) else false
    }
}