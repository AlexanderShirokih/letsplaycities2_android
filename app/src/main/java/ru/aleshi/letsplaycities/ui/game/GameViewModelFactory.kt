package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.aleshi.letsplaycities.base.game.GameViewModel
import javax.inject.Inject

class GameViewModelFactory @Inject constructor(private val gameViewModel: GameViewModel) :
    ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given `Class`.
     * Only [GameViewModel] class supported
     * @param modelClass a `Class` whose instance is requested
     * @param <T>        The type parameter for the ViewModel.
     * @return a newly created ViewModel
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return gameViewModel as T
    }

}