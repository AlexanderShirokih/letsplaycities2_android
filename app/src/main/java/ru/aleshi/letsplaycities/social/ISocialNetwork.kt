package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context

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


    fun login(activity: Activity) {
        onLogin(activity)
    }

    protected abstract fun onLogin(activity: Activity)

    abstract fun onLoggedIn(activity: Activity, access_token: String)

    fun onError() {
        callback?.onError()
    }

    open fun onLogout() {
        isInitialized = false
    }

}