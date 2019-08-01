package ru.aleshi.letsplaycities.base.game

import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.base.GamePreferences

@Module
class GameModule {

    @Provides
    fun localServer(gamePreferences: GamePreferences): LocalServer = LocalServer(gamePreferences)

    @Provides
    fun gamePreferences(application: LPSApplication): GamePreferences = application.gamePreferences
}