package ru.aleshi.letsplaycities.network

import dagger.Binds
import dagger.Module

@Module
abstract class NetworkModule {

    @Binds
    abstract fun networkPresenter(presenter: NetworkPresenterImpl): NetworkContract.Presenter
}