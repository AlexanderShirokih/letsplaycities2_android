package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.quandastudio.lpsclient.core.CredentialsProvider


object SocialNetworkManager {

    private var type: ServiceType? = null

    fun onActivityResult(mainActivity: MainActivity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        ServiceType.values().forEach {
            if (it.network.sendResult(mainActivity, requestCode, resultCode, data))
                return true
        }
        return false
    }

    fun login(serviceType: ServiceType, activity: Activity) {
        type = serviceType
        init(activity, serviceType)
        serviceType.network.login(activity)
    }

    fun init(context: Context, serviceType: ServiceType) {
        if (!serviceType.network.isInitialized)
            serviceType.network.initialize(context)
    }

    fun logout(prefs: GamePreferences, credentialsProvider: CredentialsProvider) {
        prefs.logout()
        credentialsProvider.invalidate()

        ServiceType.values().forEach {
            if (it.network.isInitialized)
                it.network.onLogout()
        }
    }

    fun registerCallback(callback: SocialNetworkLoginListener) {
        ServiceType.values().forEach {
            it.network.callback = callback
        }
    }
}