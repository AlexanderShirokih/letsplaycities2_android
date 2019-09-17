package ru.aleshi.letsplaycities.remote

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import ru.aleshi.letsplaycities.remote.internal.LPSProtocolError
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
import java.util.concurrent.TimeUnit

open class RemoteRepository constructor(var server: LPSServer) : LPSServer.ConnectionListener {

    override fun onDisconnected() {
        message.onNext(LPSClientMessage.LPSLeave())
        message.onComplete()
    }

    override fun onProtocolError(err: LPSProtocolError) = message.onError(err)

    override fun onMessage(msg: LPSClientMessage) = message.onNext(msg)

    private val message: ReplaySubject<LPSClientMessage> =
        ReplaySubject.create<LPSClientMessage>()

    private val inputMessage: Observable<LPSClientMessage> by lazy {
        message
            .subscribeOn(Schedulers.io())
            .onErrorReturn { LPSClientMessage.LPSLeave("Error: ${it.message}") }
            .publish().refCount(1, TimeUnit.SECONDS)
    }

    val words: Observable<LPSClientMessage.LPSWord> =
        inputMessage.filter { it is LPSClientMessage.LPSWord }
            .cast(LPSClientMessage.LPSWord::class.java)

    val messages: Observable<LPSClientMessage.LPSMsg> =
        inputMessage.filter { it is LPSClientMessage.LPSMsg }
            .cast(LPSClientMessage.LPSMsg::class.java)

    val leave: Maybe<LPSClientMessage.LPSLeave> =
        inputMessage.filter { it is LPSClientMessage.LPSLeave }
            .cast(LPSClientMessage.LPSLeave::class.java)
            .firstElement()

    fun connect(): Maybe<PlayerData> {
        return inputMessage
            .filter { it is LPSClientMessage.LPSLogIn }
            .cast(LPSClientMessage.LPSLogIn::class.java)
            .map {
                it.getPlayerData().apply {
                    isFriend = true
                    allowSendUID = true
                }
            }
            .firstElement()
            .doOnSubscribe {
                server.setListener(this)
                server.startServer()
            }
    }

    fun disconnect() = server.close()

    fun sendWord(wordResult: WordResult, city: String) = server.sendCity(wordResult, city)

    fun sendMessage(message: String) = server.sendMessage(message)

    fun getPlayerData() = server.getPlayerData()
}