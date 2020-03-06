package ru.aleshi.letsplaycities.ui.game

import dagger.Binds
import dagger.Module
import ru.aleshi.letsplaycities.base.combos.ComboSystemView

@Module
interface GameFragmentModule {

    @Binds
    fun provideComboView(comboBadgeView: ComboBadgeView): ComboSystemView

}