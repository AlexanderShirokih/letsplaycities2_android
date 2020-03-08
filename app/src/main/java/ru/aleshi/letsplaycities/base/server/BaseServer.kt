package ru.aleshi.letsplaycities.base.server

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import ru.aleshi.letsplaycities.base.player.User
import ru.quandastudio.lpsclient.core.LPSMessage
import java.util.concurrent.TimeUnit

/**
 * Base class representing game server logic.
 */
abstract class BaseServer(private val timeLimit: Long) {

    open fun sendFriendAcceptance(accepted: Boolean, userId: Int): Completable =
        Completable.complete()

    open val friendsRequest: Observable<LPSMessage.FriendRequest> = Observable.never()

    open val kick: Maybe<Boolean> = Maybe.never()

    //New code

    /**
     * Returns game timer, that ticks every second and completes when time is out.
     * If [timeLimit] == 0 timer won't emit any event
     */
    open fun getTimer(): Observable<Long> {
        return if (timeLimit > 0)
            Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(timeLimit + 1)
                .map { timeLimit - it }
        else
            Observable.never()
    }

    /**
     * Emits disconnections event from all users.
     */
    open fun getDisconnections(): Observable<LPSMessage.LPSLeaveMessage> = Observable.never()

    /**
     * Returns words emitted by all users
     */
    abstract fun getIncomingWords(): Observable<ResultWithCity>

    /**
     * Returns messages written by all users
     */
    abstract fun getIncomingMessages(): Observable<ResultWithMessage>

    /**
     * Call to send [city] for validation on the server.
     * @param city input city
     * @param sender city sender
     */
    abstract fun sendCity(city: String, sender: User): Observable<ResultWithCity>

    /**
     * Call to send [message] to server.
     * @param message input message
     * @param sender message sender
     */
    abstract fun sendMessage(message: String, sender: User): Completable

    /**
     * Sends friend request from player to [userId]
     * @param userId ID of user that wanna sent request
     */
    open fun sendFriendRequest(userId: Int): Completable = Completable.complete()

    /**
     * Adds [userId] to player's black list
     * @param userId user ID that should be blocked by player
     */
    open fun banUser(userId: Int): Completable = Completable.complete()

    /**
     * Disposes all server related resources and closes connection
     */
    open fun dispose() = Unit
}