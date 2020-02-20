package ru.aleshi.letsplaycities.remote.internal

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
import ru.quandastudio.lpsclient.model.util.Utils
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject

class LPSServerImpl @Inject constructor(
    private val playerData: PlayerData,
    private val connection: Connection,
    private val message: MessagePipe
) :
    Thread("LPS-Server"), LPSServer {

    private val disposable = CompositeDisposable()
    private lateinit var _listener: LPSServer.ConnectionListener


    override fun startServer() {
        start()
    }

    override fun setListener(listener: LPSServer.ConnectionListener) {
        _listener = listener
    }

    override fun getListener(): LPSServer.ConnectionListener = _listener

    override fun run() {
        try {
            connection.connect(LOCAL_PORT)

            readClientLoginRequest()
            writeLoginResponse()
            readPlayRequest()

            writePlayResponse(playerData)

            while (connection.isClientConnected()) {
                readClientMessage()
            }

        } catch (e: IOException) {
            if (e is InterruptedIOException)
                return
            if (e is LPSProtocolError)
                _listener.onProtocolError(e)
            e.printStackTrace()
        } finally {
            close()
        }

    }

    private fun readClientMessage() {
        _listener.onMessage(readMessage())
    }

    private fun writeMessage(msg: LPSMessage) {
        Completable.fromAction {
            message.write(connection.getOutputStream(), msg)
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    private fun readMessage(): LPSClientMessage {
        return Single.fromCallable {
            message.read(connection.getInputStream())
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .blockingGet()
    }

    private fun writePlayResponse(playerData: PlayerData) {
        writeMessage(
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
        )
    }

    private fun readPlayRequest() {
        val playRequest = readMessage()
        if (playRequest !is LPSClientMessage.LPSPlay || playRequest.mode != LPSClientMessage.PlayMode.RANDOM_PAIR) {
            throw LPSProtocolError()
        }
    }

    private fun writeLoginResponse() {
        //Send authorization response
        writeMessage(
            LPSMessage.LPSLoggedIn(
                newerBuild = 1,
                picHash = Utils.md5("empty")
            )
        )
    }

    private fun readClientLoginRequest() {
        val loginMsg = readMessage() as LPSClientMessage.LPSLogIn
        if (5 != loginMsg.version)
            throw LPSProtocolError("Incompatible protocol versions(4 != ${loginMsg.version})! Please, upgrade your application")

        _listener.onMessage(loginMsg)
    }

    override fun close() {
        disposable.clear()
        if (connection.isConnected()) {
            _listener.onDisconnected()
        }
        connection.close()
        if (!isInterrupted)
            interrupt()
    }

    override fun sendCity(wordResult: WordResult, city: String) {
        writeMessage(LPSMessage.LPSWordMessage(wordResult, city))
    }

    override fun sendMessage(message: String) {
        writeMessage(LPSMessage.LPSMsgMessage(message, false))
    }

    override fun getPlayerData() = playerData

    companion object {
        const val LOCAL_NETWORK_IP = "192.168.43.1"
        const val LOCAL_PORT = 8988
    }
}