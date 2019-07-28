package ru.aleshi.letsplaycities

import android.app.Application
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager

class LPSApplication :Application() {

    val gamePreferences: GamePreferences by lazy {
        GamePreferences(this)
    }

    override fun onCreate() {
        super.onCreate()
        SocialNetworkManager.init(this, ServiceType.VK)
    }
}