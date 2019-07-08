package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import ru.aleshi.letsplaycities.base.GamePreferences


object SocialNetworkManager {

    private var type: ServiceType? = null

    fun login(serviceType: ServiceType, activity: Activity) {
        type = serviceType
        init(activity, serviceType)
        serviceType.network.login(activity)
    }

    fun init(context: Context, serviceType: ServiceType) {
        if (!serviceType.network.isInitialized)
            serviceType.network.initialize(context)
    }

    fun logout(prefs: GamePreferences) {
        prefs.logout()

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