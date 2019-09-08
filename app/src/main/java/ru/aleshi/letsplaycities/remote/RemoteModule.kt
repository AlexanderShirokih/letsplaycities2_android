package ru.aleshi.letsplaycities.remote

import dagger.Binds
import dagger.Module

@Module
abstract class RemoteModule {

    @Binds
    abstract fun remotePresenter(presenter: RemotePresenter): RemoteContract.Presenter

}