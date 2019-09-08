package ru.aleshi.letsplaycities.remote

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RemoteRepository @Inject constructor(private val server: LPSServer) {

    private val inputMessage: Observable<LPSServerMessage> by lazy {
        Observable.create<LPSServerMessage> {
            server.start()
            server.setListener(object : LPSServer.ConnectionListener {
                override fun onMessage(msg: LPSServerMessage) {
                    it.onNext(msg)
                }

                override fun onProtocolError(err: LPSProtocolError) {
                    it.onError(err)
                }

                override fun onDisconnected() {
                    it.onComplete()
                }

            })
        }
            .subscribeOn(Schedulers.io())
            .onErrorReturn { LPSServerMessage.LPSDisconnectServerMessage(it.message) }
            .publish().refCount(1, TimeUnit.SECONDS)
    }

    val words: Observable<LPSServerMessage.LPSWordServerMessage> =
        inputMessage.filter { it is LPSServerMessage.LPSWordServerMessage }
            .cast(LPSServerMessage.LPSWordServerMessage::class.java)

    val messages: Observable<LPSServerMessage.LPSMsgServerMessage> =
        inputMessage.filter { it is LPSServerMessage.LPSMsgServerMessage }
            .cast(LPSServerMessage.LPSMsgServerMessage::class.java)

    val disconnect: Maybe<LPSServerMessage.LPSDisconnectServerMessage> =
        inputMessage.filter { it is LPSServerMessage.LPSDisconnectServerMessage }
            .cast(LPSServerMessage.LPSDisconnectServerMessage::class.java)
            .firstElement()

    fun connect(): Maybe<PlayerData> {
        return inputMessage
            .filter { it is LPSServerMessage.LPSConnectedMessage }
            .cast(LPSServerMessage.LPSConnectedMessage::class.java)
            .map { it.opponentData }
            .firstElement()
    }

    fun disconnect() {
        server.close()
    }

    fun sendWord(wordResult: WordResult, city: String) {
        server.sendCity(wordResult, city)
    }
}