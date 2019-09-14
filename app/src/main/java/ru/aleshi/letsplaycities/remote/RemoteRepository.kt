package ru.aleshi.letsplaycities.remote

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import ru.aleshi.letsplaycities.remote.internal.LPSProtocolError
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.aleshi.letsplaycities.remote.internal.LPSServerMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
import java.util.concurrent.TimeUnit

open class RemoteRepository constructor(var server: LPSServer) : LPSServer.ConnectionListener {

    override fun onDisconnected() {
        message.onNext(LPSServerMessage.LPSLeaveServerMessage(""))
        message.onComplete()
    }

    override fun onProtocolError(err: LPSProtocolError) = message.onError(err)

    override fun onMessage(msg: LPSServerMessage) = message.onNext(msg)

    private val message: ReplaySubject<LPSServerMessage> =
        ReplaySubject.create<LPSServerMessage>()

    private val inputMessage: Observable<LPSServerMessage> by lazy {
        message
            .subscribeOn(Schedulers.io())
            .onErrorReturn { LPSServerMessage.LPSLeaveServerMessage(it.message) }
            .publish().refCount(1, TimeUnit.SECONDS)
    }

    val words: Observable<LPSServerMessage.LPSWordServerMessage> =
        inputMessage.filter { it is LPSServerMessage.LPSWordServerMessage }
            .cast(LPSServerMessage.LPSWordServerMessage::class.java)

    val messages: Observable<LPSServerMessage.LPSMsgServerMessage> =
        inputMessage.filter { it is LPSServerMessage.LPSMsgServerMessage }
            .cast(LPSServerMessage.LPSMsgServerMessage::class.java)

    val leave: Maybe<LPSServerMessage.LPSLeaveServerMessage> =
        inputMessage.filter { it is LPSServerMessage.LPSLeaveServerMessage }
            .cast(LPSServerMessage.LPSLeaveServerMessage::class.java)
            .firstElement()

    fun connect(): Maybe<PlayerData> {
        return inputMessage
            .filter { it is LPSServerMessage.LPSConnectedMessage }
            .cast(LPSServerMessage.LPSConnectedMessage::class.java)
            .map { it.opponentData }
            .firstElement()
            .doOnSubscribe {
                server.setListener(this)
                server.startServer()
            }
    }

    fun disconnect() {
        server.close()
    }

    fun sendWord(wordResult: WordResult, city: String) {
        server.sendCity(wordResult, city)
    }

    fun getPlayerData() = server.getPlayerData()
}