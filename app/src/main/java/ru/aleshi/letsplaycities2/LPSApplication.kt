package ru.aleshi.letsplaycities2

import android.app.Application
import ru.aleshi.letsplaycities2.base.BanManager
import ru.aleshi.letsplaycities2.base.GamePreferences

class LPSApplication : Application() {

    val gamePreferences: GamePreferences by lazy {
        GamePreferences(this)
    }

    val banManager: BanManager by lazy {
        BanManager(gamePreferences)
    }
}