package ru.aleshi.letsplaycities.base.game

import dagger.Binds
import dagger.Module

@Module(includes = [GameModule.SubModule::class])
class GameModule {

    @Module
    interface SubModule {

        @Binds
        fun presenter(presenter: GamePresenter) : GameContract.Presenter

    }
}