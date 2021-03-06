package ru.aleshi.letsplaycities.base.game

import dagger.Binds
import dagger.Module
import ru.aleshi.letsplaycities.base.dictionary.DictionaryModule

@Module
interface GameModule {

    @Binds
    fun presenter(presenter: GamePresenter): GameContract.Presenter

}