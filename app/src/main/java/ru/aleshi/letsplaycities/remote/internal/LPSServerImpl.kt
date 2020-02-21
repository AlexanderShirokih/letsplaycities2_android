package ru.aleshi.letsplaycities.remote.internal

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
import ru.quandastudio.lpsclient.model.util.Utils
import java.net.SocketException
import javax.inject.Inject

class LPSServerImpl @Inject constructor(
    private val playerData: PlayerData,
    private val connection: Connection,
    private val message: MessagePipe
) : LPSServer {

    private val disposable = CompositeDisposable()
    private lateinit var _listener: LPSServer.ConnectionListener


    override fun startServer() {
        disposable.add(
            Observable.create<Unit> {
                try {
                    connection.connect(LOCAL_PORT)
                    it.onNext(Unit)
                } catch (e: Exception) {
                    it.tryOnError(e)
                }
            }
                .subscribeOn(Schedulers.io())
                .flatMap { readClientLoginRequest() }
                .flatMap { writeLoginResponse() }
                .flatMap { readPlayRequest() }
                .flatMap { writePlayResponse(playerData) }
                .flatMap { readAllMessages() }
                .subscribe({
                }, { e ->
                    if (e is LPSProtocolError)
                        _listener.onProtocolError(e)
                    internalClose()
                }, {
                    internalClose()
                })
        )
    }

    override fun setListener(listener: LPSServer.ConnectionListener) {
        _listener = listener
    }

    override fun getListener(): LPSServer.ConnectionListener = _listener

    private fun readAllMessages(): Observable<LPSClientMessage> {
        return Observable.create<LPSClientMessage> {
            try {
                while (connection.isClientConnected()) {
                    val msg = message.read(connection.getInputStream())
                    if (msg == null) {
                        _listener.onMessage(LPSClientMessage.LPSLeave())
                        break
                    } else
                        _listener.onMessage(msg)
                }
                if (!it.isDisposed)
                    it.onComplete()
            } catch (e: Exception) {
                if (e is SocketException)
                    it.onComplete()
                else {
                    it.tryOnError(e)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun writeMessage(msg: LPSMessage): Completable {
        return Completable.fromAction {
            message.write(connection.getOutputStream(), msg)
        }.subscribeOn(Schedulers.io())
    }

    private fun readMessage(): Observable<LPSClientMessage> {
        return Observable.create<LPSClientMessage> {
            try {
                val msg = message.read(connection.getInputStream())
                if (msg == null) {
                    it.onNext(LPSClientMessage.LPSLeave())
                    it.onComplete()
                } else
                    it.onNext(msg)
            } catch (e: Exception) {
                it.tryOnError(e)
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun writePlayResponse(playerData: PlayerData): Observable<Unit> {
        return writeMessage(
            LPSMessage.LPSPlayMessage(
                youStarter = false,
                canReceiveMessages = playerData.canReceiveMessages,
                login = playerData.authData.login,
                clientVersion = playerData.versionInfo.versionName,
                clientBuild = playerData.versionInfo.versionCode,
                isFriend = true,
                oppUid = playerData.authData.credentials.userId,
                authType = playerData.authData.snType,
                pictureHash = playerData.pictureHash
            )
        ).andThen(Observable.just(Unit))
    }

    private fun readPlayRequest(): Observable<LPSClientMessage> {
        return readMessage()
            .flatMap { playRequest ->
                if (playRequest !is LPSClientMessage.LPSPlay || playRequest.mode != LPSClientMessage.PlayMode.RANDOM_PAIR) {
                    Observable.error(LPSProtocolError())
                } else Observable.just(playRequest)
            }
    }

    private fun writeLoginResponse(): Observable<Unit> {
        //Send authorization response
        return writeMessage(
            LPSMessage.LPSLoggedIn(
                newerBuild = 1,
                picHash = Utils.md5("empty")
            )
        ).andThen(Observable.just(Unit))
    }

    private fun readClientLoginRequest(): Observable<LPSClientMessage.LPSLogIn> {
        return readMessage().cast(LPSClientMessage.LPSLogIn::class.java)
            .flatMap { loginMsg ->
                if (5 != loginMsg.version)
                    Observable.error(LPSProtocolError("Incompatible protocol versions(5 != ${loginMsg.version})! Please, upgrade your application"))
                else
                    Observable.just(loginMsg)
            }
            .doOnNext(_listener::onMessage)
    }

    private fun internalClose() {
        if (connection.isConnected()) {
            _listener.onDisconnected()
        }
        connection.close()
    }

    override fun close() {
        internalClose()
        disposable.clear()
    }

    override fun sendCity(wordResult: WordResult, city: String) {
        disposable.add(writeMessage(LPSMessage.LPSWordMessage(wordResult, city)).subscribe())
    }

    override fun sendMessage(message: String) {
        disposable.add(writeMessage(LPSMessage.LPSMsgMessage(message, false)).subscribe())
    }

    override fun getPlayerData() = playerData

    companion object {
        const val LOCAL_NETWORK_IP = "192.168.43.1"
        const val LOCAL_PORT = 8988
    }
}