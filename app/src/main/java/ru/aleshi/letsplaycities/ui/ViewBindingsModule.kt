package ru.aleshi.letsplaycities.ui

import dagger.Binds
import dagger.Module
import ru.aleshi.letsplaycities.base.mainmenu.MainMenuContract
import ru.aleshi.letsplaycities.ui.mainmenu.MainMenuFragment

@Module
interface ViewBindingsModule {

    @Binds
    fun view(fragment: MainMenuFragment): MainMenuContract.MainMenuView

}