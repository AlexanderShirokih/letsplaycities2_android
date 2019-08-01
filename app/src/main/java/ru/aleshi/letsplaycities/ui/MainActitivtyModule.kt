package ru.aleshi.letsplaycities.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [ViewModelModule::class])
abstract class MainActivityModule {

    @ContributesAndroidInjector(modules = [FragmentsModule::class])
    abstract fun contributeMainActivity(): MainActivity
}