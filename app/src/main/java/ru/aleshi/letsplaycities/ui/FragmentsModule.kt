package ru.aleshi.letsplaycities.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.aleshi.letsplaycities.base.dictionary.DictionaryModule
import ru.aleshi.letsplaycities.base.game.GameModule
import ru.aleshi.letsplaycities.ui.mainmenu.MainMenuFragment

@Module
abstract class FragmentsModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [GameModule::class, DictionaryModule::class])
    abstract fun contributeMainMenuFragment(): MainMenuFragment

}