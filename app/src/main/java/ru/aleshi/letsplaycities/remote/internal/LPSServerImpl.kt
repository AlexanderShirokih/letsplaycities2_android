package ru.aleshi.letsplaycities.remote.internal

import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject

class LPSServerImpl @Inject constructor(
    private val playerData: Single<PlayerData>,
    private val connection: Connection,
    private val message: MessagePipe
) :
    Thread("LPS-Server"), LPSServer {

    private val disposable = CompositeDisposable()
    private lateinit var cachedPlayerData: PlayerData
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
            cachedPlayerData = playerData.blockingGet()
            connection.connect(LOCAL_PORT)

            readClientLoginRequest()
            writeLoginResponse()
            readPlayRequest()

            writePlayResponse(cachedPlayerData)

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

    private fun writeMessage(msg: LPSMessage) = message.write(connection.getOutputStream(), msg)

    private fun readMessage() = message.read(connection.getInputStream())

    private fun writePlayResponse(playerData: PlayerData) {
        writeMessage(
            LPSMessage.LPSPlayMessage(
                youStarter = false,
                canReceiveMessages = playerData.canReceiveMessages,
                login = playerData.authData.login,
                clientVersion = playerData.clientVersion,
                clientBuild = playerData.clientBuild,
                isFriend = true,
                oppUid = playerData.authData.userID,
                snUID = playerData.authData.snUID,
                authType = playerData.authData.snType
            ).setAvatar(playerData.avatar)
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
                userId = 1,
                accHash = "-remote-"
            )
        )
    }

    private fun readClientLoginRequest() {
        val loginMsg = readMessage() as LPSClientMessage.LPSLogIn
        if (4 != loginMsg.version)
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

    override fun getPlayerData() = cachedPlayerData

    companion object {
        const val LOCAL_NETWORK_IP = "192.168.43.1"
        const val LOCAL_PORT = 8988
    }
}