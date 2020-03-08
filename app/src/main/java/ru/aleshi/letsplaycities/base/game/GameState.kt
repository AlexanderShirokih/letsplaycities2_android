package ru.aleshi.letsplaycities.base.game

/**
 * Represents game states
 */
sealed class GameState {

    /**
     * Default state before game starts.
     */
    object Initial : GameState()

    /**
     * Applied when game start to load dictionary update.
     * Should be applied before [LoadingDictionary] state.
     */
    object CheckingForUpdates : GameState()

    /**
     * Applied when started loading updates
     * @param progressPercent percent of loaded dictionary clamped in [0..100]
     */
    class LoadingUpdate(val progressPercent: Int) : GameState()

    /**
     * Applied when game starts parsing dictionary.
     */
    object LoadingDictionary : GameState()

    /**
     * Applied when starting sequence was completed successfully.
     */
    object Started : GameState()

    /**
     * Applied when something went wrong on start time.
     * @param error [Throwable] describing error.
     */
    class Error(val error: Throwable) : GameState()

    /**
     * Applies when games finishes by any reason.
     * @param event reason why game hash finished.
     */
    class Finish(val event: FinishEvent) : GameState()
}