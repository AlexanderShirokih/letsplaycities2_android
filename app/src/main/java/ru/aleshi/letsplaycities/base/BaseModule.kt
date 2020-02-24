package ru.aleshi.letsplaycities.base

import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.AppVersionInfo
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.base.dictionary.DictionaryModule
import ru.quandastudio.lpsclient.model.VersionInfo

@Module(includes = [DictionaryModule::class])
class BaseModule {

    @Provides
    fun gamePreferences(application: LPSApplication): GamePreferences = application.gamePreferences

    @AppVersionInfo
    @Provides
    fun versionInfo() = VersionInfo(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

}