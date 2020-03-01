package ru.aleshi.letsplaycities.ui.game

import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.base.combos.ComboSystemView

@Module
class GameFragmentModule {

    @Provides
    fun provideComboView(gameFragment: GameFragment): ComboSystemView {
        return ComboBadgeView(gameFragment)
    }

}