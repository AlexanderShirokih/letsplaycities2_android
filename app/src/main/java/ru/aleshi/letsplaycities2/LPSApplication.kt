package ru.aleshi.letsplaycities2

import android.app.Application
import ru.aleshi.letsplaycities2.base.GamePreferences

class LPSApplication : Application() {

    val gamePreferences: GamePreferences by lazy {
        GamePreferences(this)
    }
}