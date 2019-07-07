package ru.aleshi.letsplaycities

import android.app.Application
import ru.aleshi.letsplaycities.base.BanManager
import ru.aleshi.letsplaycities.base.GamePreferences

class LPSApplication : Application() {

    val gamePreferences: GamePreferences by lazy {
        GamePreferences(this)
    }

    val banManager: BanManager by lazy {
        BanManager(gamePreferences)
    }
}