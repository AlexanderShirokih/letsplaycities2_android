package ru.aleshi.letsplaycities.base.server

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import ru.aleshi.letsplaycities.base.player.User
import ru.quandastudio.lpsclient.core.LPSMessage

/**
 * Base class representing game server logic.
 */
abstract class BaseServer {

    open fun sendFriendAcceptance(accepted: Boolean, userId: Int): Completable =
        Completable.complete()

    open val leave: Maybe<Boolean> = Maybe.never()

    open val timeout: Maybe<LPSMessage> = Maybe.never()

    open val friendsRequest: Observable<LPSMessage.FriendRequest> = Observable.never()

    open val kick: Maybe<Boolean> = Maybe.never()

    //New code

    /**
     * Adds [userId] to player's black list
     * @param userId user ID that should be blocked by player
     */
    open fun banUser(userId: Int): Completable = Completable.complete()

    /**
     * Sends friend request from player to [userId]
     * @param userId ID of user that wanna sent request
     */
    open fun sendFriendRequest(userId: Int): Completable = Completable.complete()

    /**
     * Returns time limit per move in seconds
     */
    abstract fun getTimeLimit(): Long

    /**
     * Returns words emitted by all users
     */
    abstract fun getWordsResult(): Observable<ResultWithCity>

    /**
     * Returns messages written by all users
     */
    abstract fun getIncomingMessages(): Observable<ResultWithMessage>

    /**
     * Call to send [city] for validation on the server.
     * @param city input city
     * @param sender city sender
     */
    abstract fun sendCity(city: String, sender: User): Completable

    /**
     * Call to send [message] to server.
     * @param message input message
     * @param sender message sender
     */
    abstract fun sendMessage(message: String, sender: User): Completable


    /**
     * Disposes all server related resources and closes connection
     */
    open fun dispose() = Unit
}