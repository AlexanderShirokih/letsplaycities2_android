package ru.aleshi.letsplaycities

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjectionModule
import ru.aleshi.letsplaycities.base.dictionary.DictionaryModule
import ru.aleshi.letsplaycities.ui.MainActivityModule

@Module(includes = [AndroidInjectionModule::class, MainActivityModule::class, DictionaryModule::class])
abstract class AppModule {

    @Binds
    abstract fun context(application: Application): Context

    @Binds
    abstract fun lpsApplication(application: LPSApplication): Application

}