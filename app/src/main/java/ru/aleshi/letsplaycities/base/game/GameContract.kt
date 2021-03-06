package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.Flowable
import ru.aleshi.letsplaycities.base.player.User
import ru.quandastudio.lpsclient.core.LPSMessage

/**
 * Interface that holds ViewModel and Presenter interfaces to game control.
 */
interface GameContract {

    /**
     * ViewModel interface for controlling game UI state.
     */
    interface ViewModel {

        /**
         * Called to update current game state.
         * @param state new [GameState] to bind.
         */
        fun updateState(state: GameState)

        /**
         * Called on every game timer tick to update UI time.
         * @param time current time label
         */
        fun updateTime(time: String)

        /**
         * Called to switch active user highlight and update other user info.
         * @param prev user that has finished move or `null` if there are no moves before.
         * @param next user whose turn has come
         */
        fun switchUser(prev: User?, next: User)

        /**
         * Called to put game entity (city or message) to the screen.
         */
        fun putGameEntity(entity: GameEntity)

        /**
         * Called when received friend request
         */
        fun onFriendRequestResult(type: LPSMessage.LPSFriendRequest)

        /**
         * Call to dispose all resources
         */
        fun dispose()

    }

    /**
     * Presenter interface which is responsible for controlling game.
     */
    interface Presenter {
        /**
         * Call to begin game sequence.
         * @param viewModel ViewModel Instance
         * @param session current [GameSession] instance
         */
        fun start(viewModel: ViewModel, session: GameSession)

        /**
         * Call to send input from user to players.
         * @return [Flowable] that emits [WordCheckingResult] or [Flowable.empty] if current user
         * not yet defined or can't make move.
         */
        fun onUserInput(input: String): Flowable<WordCheckingResult>

        /**
         * Call to send user's message to server.
         */
        fun onMessage(message: String): Completable

        /**
         * Call when player wants to surrender
         */
        fun onSurrender()

        /**
         * Sends friend request to [userId] over game server.
         * @param userId id of user that we want to add to friends
         */
        fun sendFriendRequest(userId: Int): Completable

        /**
         * Sends ban message to [userId] over game server.
         * @param userId id of user that we want to ban
         */
        fun banUser(userId: Int): Completable

        /**
         * Call to dispose resources.
         */
        fun dispose()

        /**
         * Starts searching hint for player.
         */
        fun onPlayerHint(): Completable

        /**
         * Returns current game session
         */
        fun getCurrentSession(): GameSession

    }

}