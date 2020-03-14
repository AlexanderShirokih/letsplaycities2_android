package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import com.google.android.material.snackbar.Snackbar
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.GameState
import javax.inject.Inject

/**
 * Shows user states info on [Snackabar]
 */
class GameStateNotifier @Inject constructor(
    gameFragment: GameFragment,
    private val context: Context
) {

    /**
     * Lazy [Snackbar] instance
     */
    private val snackbar: Snackbar by lazy {
        Snackbar.make(gameFragment.requireView(), "", Snackbar.LENGTH_SHORT)
    }

    fun showState(state: GameState) {
        when (state) {
            GameState.CheckingForUpdates -> showMessage(context.getString(R.string.checking_for_updates))
            is GameState.LoadingUpdate -> showMessage(
                context.getString(
                    R.string.updating_dictionary,
                    state.progressPercent
                )
            )
            GameState.LoadingDictionary -> showMessage(context.getString(R.string.loading_dictionary))
            GameState.Started -> hide()
            is GameState.Error -> showMessage(
                context.getString(
                    R.string.unk_error,
                    state.error.toString()
                ), false
            )
            is GameState.Finish, GameState.Started -> hide()
        }
    }

    private fun showMessage(message: String, isIndeterminate: Boolean = true) {
        snackbar.apply {
            setText(message)
            duration = if (isIndeterminate) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
            if (!isShownOrQueued) show()
        }
    }

    fun hide() {
        snackbar.dismiss()
    }

}