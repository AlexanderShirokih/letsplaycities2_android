package ru.aleshi.letsplaycities.ui

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import ru.aleshi.letsplaycities.social.SocialModule

@Module(includes = [ViewModelModule::class])
abstract class MainActivityModule {

    @ContributesAndroidInjector(modules = [FragmentsModule::class, MainActivitySubModule::class, SocialModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @Module
    class MainActivitySubModule {
        @Provides
        fun asyncActivity(mainActivity: MainActivity): BaseAsyncActivity = mainActivity
    }
}