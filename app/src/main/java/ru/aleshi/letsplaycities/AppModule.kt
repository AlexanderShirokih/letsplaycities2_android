package ru.aleshi.letsplaycities

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjectionModule
import ru.aleshi.letsplaycities.base.game.GameModule
import ru.aleshi.letsplaycities.service.ServiceModule
import ru.aleshi.letsplaycities.ui.MainActivityModule

@Module(includes = [AndroidInjectionModule::class, ServiceModule::class, MainActivityModule::class])
abstract class AppModule {

    @Binds
    abstract fun context(application: Application): Context

    @Binds
    abstract fun lpsApplication(application: LPSApplication): Application

}