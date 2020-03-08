package ru.aleshi.letsplaycities.remote

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.player.UserIdIdentity
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.base.server.ResultWithMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.WordResult
import javax.inject.Inject

class RemoteServer @Inject constructor(private val remoteRepository: RemoteRepository) :
    BaseServer(timeLimit = 92L) {

    /**
     * Used to redirect input messages to output.
     */
    private var messages: ReplaySubject<ResultWithMessage> = ReplaySubject.create()

    /**
     * Returns words emitted by all users
     */
    override fun getIncomingWords(): Observable<ResultWithCity> {
        return remoteRepository.words.map {
            ResultWithCity(
                wordResult = WordResult.RECEIVED,
                city = it.word,
                identity = UserIdIdentity(
                    remoteRepository.getOppData()?.authData?.credentials?.userId ?: 0
                )
            )
        }
    }

    /**
     * Returns messages written by all users
     */
    override fun getIncomingMessages(): Observable<ResultWithMessage> {
        return messages.mergeWith(remoteRepository.messages
            .map {
                ResultWithMessage(
                    it.msg,
                    UserIdIdentity(
                        remoteRepository.getOppData()?.authData?.credentials?.userId ?: 0
                    )
                )
            })
    }

    /**
     * Closes remote connection
     */
    override fun dispose() {
        remoteRepository.disconnect()
    }

    /**
     * Sends [city] for validation on the server.
     * @param city input city
     * @param sender city sender
     */
    override fun sendCity(city: String, sender: User): Observable<ResultWithCity> {
        return Completable.fromAction {
            remoteRepository.sendWord(
                wordResult = WordResult.RECEIVED,
                city = city,
                ownerId = sender.credentials.userId
            )
        }
            .andThen(
                Observable.just(
                    ResultWithCity(
                        wordResult = WordResult.ACCEPTED,
                        city = city,
                        identity = UserIdIdentity(sender.credentials.userId)
                    )
                )
            )
    }

    /**
     * Sends message to remote server
     * @param message input message
     * @param sender message sender
     */
    override fun sendMessage(message: String, sender: User): Completable {
        return Completable.fromAction {
            remoteRepository.sendMessage(
                message = message,
                ownerId = sender.credentials.userId
            )
            messages.onNext(
                ResultWithMessage(
                    message = message,
                    identity = UserIdIdentity(sender.credentials.userId)
                )
            )
        }
    }

    /**
     * Emits event when user disconnects by any reason.
     */
    override fun getDisconnections(): Observable<LPSMessage.LPSLeaveMessage> =
        remoteRepository.leave.map {
            LPSMessage.LPSLeaveMessage(
                !it.reason.isNullOrEmpty(),
                remoteRepository.getOppData()?.authData?.credentials?.userId ?: 0
            )
        }.toObservable()
}