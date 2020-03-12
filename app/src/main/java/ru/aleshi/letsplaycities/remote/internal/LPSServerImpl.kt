package ru.aleshi.letsplaycities.remote.internal

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.remote.internal.LPSServer.ConnectionListener
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.*
import ru.quandastudio.lpsclient.model.util.Utils
import java.net.SocketException
import javax.inject.Inject

/**
 * Implementation of [LPSServer].
 * Provides game server logic for two users.
 * @param playerData [PlayerData] of user who hosts the game
 * @param connection transport connection for transferring data between server and client
 * @param message message pipe for [de-]serializing input and output messages
 */
class LPSServerImpl @Inject constructor(
    private val playerData: PlayerData,
    private val connection: Connection,
    private val message: MessagePipe
) : LPSServer {

    /**
     * Reference to opponent's [PlayerData]. `null` while not connected.
     */
    private var _oppData: PlayerData? = null

    /**
     * Disposable that manages all server subscriptions
     */
    private val disposable = CompositeDisposable()

    /**
     * Listener that listens connection status
     */
    private lateinit var _listener: ConnectionListener

    /**
     * Starts the server and begins connection sequence.
     * When connected emits listener events.
     */
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

    /**
     * Installs the collection listener
     * @param listener listener to be installed
     */
    override fun setListener(listener: ConnectionListener) {
        _listener = listener
    }

    /**
     * Returns current [ConnectionListener]
     */
    override fun getListener(): ConnectionListener = _listener

    /**
     * Reads messages from client.
     * @return [Observable] of [LPSClientMessage] that emits all messages from client. When error
     * occurred while reading message will be emitted single [LPSClientMessage.LPSLeave]
     * message and then completes. Observable will completed when connection was closed.
     * Also completes when raised [SocketException] and emits error on any other exceptions.
     */
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

    /**
     * Writes message [msg] to out connection (sends them to client).
     */
    private fun writeMessage(msg: LPSMessage): Completable {
        return Completable.fromAction {
            message.write(connection.getOutputStream(), msg)
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Reads all kind of message from client and emits it.
     * When error occurred while reading message will be emitted single [LPSClientMessage.LPSLeave]
     * message and then completes.
     * @return [Observable] that emits single message from client to `onNext`. This [Observable] will
     * be completed only when
     */
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

    /**
     * Sends play response with [playerData] of player who hosts this game.
     * @return [Observable] emitting [Unit] when message was sent.
     */
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

    /**
     * Reads client's play request and return it.
     * @return [Observable] if [LPSClientMessage]
     */
    private fun readPlayRequest(): Observable<LPSClientMessage> {
        return readMessage()
            .flatMap { playRequest ->
                if (playRequest !is LPSClientMessage.LPSPlay || playRequest.mode != LPSClientMessage.PlayMode.RANDOM_PAIR) {
                    Observable.error(LPSProtocolError())
                } else Observable.just(playRequest)
            }
    }

    /**
     * Sends authorization response to client.
     * @return [Observable] emitting [Unit] when message was sent
     */
    private fun writeLoginResponse(): Observable<Unit> {
        return writeMessage(
            LPSMessage.LPSLoggedIn(
                newerBuild = 1,
                picHash = Utils.md5("empty")
            )
        ).andThen(Observable.just(Unit))
    }

    /**
     * Read client login request ([LPSClientMessage.LPSLogIn]). Saves login into to [_oppData].
     * @return input [LPSClientMessage.LPSLogIn] message.
     */
    private fun readClientLoginRequest(): Observable<LPSClientMessage.LPSLogIn> {
        return readMessage().cast(LPSClientMessage.LPSLogIn::class.java)
            .flatMap { loginMsg ->
                if (5 != loginMsg.version)
                    Observable.error(LPSProtocolError("Incompatible protocol versions(5 != ${loginMsg.version})! Please, upgrade your application"))
                else
                    Observable.just(loginMsg)
            }
            .doOnNext {
                _oppData = PlayerData(
                    authData = AuthData(
                        login = it.login,
                        credentials = Credentials(it.uid, ""),
                        snType = AuthType.Native
                    ),
                    versionInfo = VersionInfo(it.clientVersion, it.clientBuild),
                    pictureHash = it.picHash,
                    canReceiveMessages = it.canReceiveMessages,
                    isFriend = true
                )
            }
            .doOnNext(_listener::onMessage)
    }

    /**
     * Closes server connection and call `onDisconnected` on connection listener.
     */
    private fun internalClose() {
        if (connection.isConnected()) {
            _listener.onDisconnected()
        }
        connection.close()
    }

    /**
     * Closes server connection, disposes all subscribers.
     */
    override fun close() {
        internalClose()
        disposable.clear()
    }

    /**
     * Sends [city] to client
     * @param wordResult result of word validation
     * @param city input city
     * @param ownerId ID of user who sends this city
     */
    override fun sendCity(wordResult: WordResult, city: String, ownerId: Int) {
        disposable.add(
            writeMessage(
                LPSMessage.LPSWordMessage(
                    wordResult,
                    city,
                    ownerId
                )
            ).subscribe()
        )
    }

    /**
     * Sends [message] to client
     * @param message message
     * @param ownerId ID of user who sends this message
     */
    override fun sendMessage(message: String, ownerId: Int) {
        disposable.add(writeMessage(LPSMessage.LPSMsgMessage(message, false, ownerId)).subscribe())
    }

    /**
     * Returns player data of server owner (who creates the host)
     * @return [PlayerData] of server's owner
     */
    override fun getPlayerData() = playerData

    /**
     * Returns player data of connected client
     * @return [PlayerData] of connected client or `null` if client not connected yet
     */
    override fun getOppData() = _oppData

    companion object {
        const val LOCAL_NETWORK_IP = "192.168.43.1"
        const val LOCAL_PORT = 8988
    }
}