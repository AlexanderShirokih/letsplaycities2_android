package ru.aleshi.letsplaycities.base.game

import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.AppVersionInfo
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.quandastudio.lpsclient.model.VersionInfo

@Module
class GameModule {

    @Provides
    fun localServer(gamePreferences: GamePreferences): LocalServer = LocalServer(gamePreferences)

    @Provides
    fun gamePreferences(application: LPSApplication): GamePreferences = application.gamePreferences

    @AppVersionInfo
    @Provides
    fun versionInfo() = VersionInfo(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
}