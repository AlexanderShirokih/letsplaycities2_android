package ru.aleshi.letsplaycities.remote

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import ru.aleshi.letsplaycities.remote.internal.LPSProtocolError
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.model.*
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
                PlayerData(
                    authData = AuthData(it.login, AuthType.Native, Credentials(it.uid, it.hash)),
                    isFriend = true,
                    canReceiveMessages = it.canReceiveMessages,
                    versionInfo = VersionInfo(it.clientVersion, it.clientBuild),
                    pictureHash = it.picHash
                )
            }
            .firstElement()
            .doOnSubscribe {
                server.setListener(this)
                server.startServer()
            }
    }

    fun disconnect() = server.close()

    fun sendWord(wordResult: WordResult, city: String, ownerId: Int) =
        server.sendCity(wordResult, city, ownerId)

    fun sendMessage(message: String, ownerId: Int) = server.sendMessage(message, ownerId)

    fun getPlayerData() = server.getPlayerData()

    fun getOppData() = server.getOppData()
}