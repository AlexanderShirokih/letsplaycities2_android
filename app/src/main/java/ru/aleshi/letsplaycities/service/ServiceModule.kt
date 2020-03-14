package ru.aleshi.letsplaycities.service

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.aleshi.letsplaycities.network.LpsRepositoryModule

@Module(includes = [LpsRepositoryModule::class])
interface ServiceModule {

    @ContributesAndroidInjector
    fun injectFirebaseService(): MyFirebaseMessagingService

}