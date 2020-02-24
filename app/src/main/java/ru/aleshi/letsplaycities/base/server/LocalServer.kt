package ru.aleshi.letsplaycities.base.server

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.player.UserIdIdentity
import ru.quandastudio.lpsclient.model.WordResult
import javax.inject.Inject

/**
 * Implementation of [BaseServer] for local games.
 * @param gamePreferences [GamePreferences] instance for getting time limit
 */
class LocalServer @Inject constructor(private val gamePreferences: GamePreferences) : BaseServer() {

    /**
     * Used to redirect input words to output.
     */
    private val cities: PublishSubject<ResultWithCity> = PublishSubject.create()

    /**
     * Used to redirect input messages to output.
     */
    private val messages: PublishSubject<ResultWithMessage> = PublishSubject.create()

    /**
     * Returns words emitted by all users
     */
    override fun getWordsResult(): Observable<ResultWithCity> = cities

    /**
     * Returns messages written by all users
     */
    override fun getIncomingMessages(): Observable<ResultWithMessage> = messages

    /**
     * Call to send [city] for validation on the server.
     * @param city input city
     * @param sender city sender
     */
    override fun sendCity(city: String, sender: User): Completable {
        // We trust our local users
        return Completable.fromAction {
            cities.onNext(
                ResultWithCity(
                    wordResult = WordResult.ACCEPTED,
                    city = city,
                    identity = UserIdIdentity(sender.credentials.userId)
                )
            )
        }
    }

    /**
     * Call to send [message] to server.
     * @param message input message
     * @param sender message sender
     */
    override fun sendMessage(message: String, sender: User): Completable {
        return Completable.fromAction {
            messages.onNext(
                ResultWithMessage(
                    message = message,
                    identity = UserIdIdentity(sender.credentials.userId)
                )
            )
        }
    }

    /**
     * Returns time limit per move in seconds
     * @return the time limit
     */
    override fun getTimeLimit(): Long = gamePreferences.getTimeLimit()

}