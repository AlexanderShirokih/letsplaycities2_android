package ru.aleshi.letsplaycities.remote.internal

import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import ru.quandastudio.lpsclient.core.LPSMessageReader
import ru.quandastudio.lpsclient.core.LPSMessageWriter
import ru.quandastudio.lpsclient.core.LPSv3Tags
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.AuthType
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject

class LPSServerImpl @Inject constructor(
    private val playerData: Single<PlayerData>,
    private val connection: Connection
) :
    Thread("LPS-Server"), LPSServer {

    private val disposable = CompositeDisposable()
    private lateinit var cachedPlayerData: PlayerData
    private var writer: LPSMessageWriter? = null
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
            writer = LPSMessageWriter(connection.getOutputStream())

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
        _listener.onMessage(LPSServerMessage.from(reader()))
    }

    private fun writePlayResponse(playerData: PlayerData) {
        val playMsg = writer()
            .writeBool(LPSv3Tags.ACTION_JOIN, false)
            .writeBool(LPSv3Tags.S_CAN_REC_MSG, playerData.canReceiveMessages)
            .writeString(LPSv3Tags.OPP_LOGIN, playerData.authData.login)
            .writeString(LPSv3Tags.OPP_CLIENT_VERSION, playerData.clientVersion)
            .writeChar(LPSv3Tags.OPP_CLIENT_BUILD, playerData.clientBuild)
            .writeBool(LPSv3Tags.OPP_IS_FRIEND, true)
            .writeInt(LPSv3Tags.S_OPP_UID, playerData.authData.userID)
            .writeString(LPSv3Tags.S_OPP_SNUID, playerData.authData.snUID)
            .writeByte(LPSv3Tags.S_OPP_SN, playerData.authData.snType.ordinal.toByte())

        playerData.avatar?.run { playMsg.writeBytes(LPSv3Tags.S_AVATAR_PART0, this) }

        playMsg.buildAndFlush(asServer = true)
    }

    private fun readPlayRequest() {
        val playRequest = reader()
        if (playRequest.getMasterTag() != LPSv3Tags.ACTION_PLAY || playRequest.readBoolean(LPSv3Tags.ACTION_PLAY)) {
            throw LPSProtocolError()
        }
    }

    private fun writeLoginResponse() {
        //Send authorization response
        writer()
            .writeBool(LPSv3Tags.ACTION_LOGIN_RESULT, true)
            .writeChar(LPSv3Tags.NEWER_BUILD, 1)
            .writeInt(LPSv3Tags.S_UID, 1)
            .writeString(LPSv3Tags.S_ACC_HASH, "-remote-")
            .buildAndFlush(asServer = true)
    }

    private fun readClientLoginRequest() {
        val loginMsg = reader()
        val version = loginMsg.readByte(LPSv3Tags.ACTION_LOGIN).toInt()
        if (4 != version)
            throw LPSProtocolError("Incompatible protocol versions(4 != $version)! Please, upgrade your application")

        val login = loginMsg.readString(LPSv3Tags.LOGIN)
        val snUID = loginMsg.readString(LPSv3Tags.SN_UID)
        val snType = AuthType.from(loginMsg.readByte(LPSv3Tags.SN).toInt())
        val canReceiveMessages = loginMsg.readBoolean(LPSv3Tags.CAN_REC_MSG)
        val avatar: ByteArray? = loginMsg.optBytes(LPSv3Tags.AVATAR_PART0)
        val clientBuild = loginMsg.readChar(LPSv3Tags.CLIENT_BUILD)
        val clientVersion = loginMsg.readString(LPSv3Tags.CLIENT_VERSION)

        _listener.onMessage(
            LPSServerMessage.LPSConnectedMessage(
                PlayerData(
                    AuthData(
                        login,
                        snUID,
                        snType,
                        ""
                    )
                ).apply {
                    this.canReceiveMessages = canReceiveMessages
                    this.avatar = avatar
                    this.clientVersion = clientVersion
                    this.clientBuild = clientBuild
                    isFriend = true
                    allowSendUID = true
                })
        )
    }

    private fun writer(): LPSMessageWriter = writer!!

    private fun reader(): LPSMessageReader = LPSMessageReader(connection.getInputStream())

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
        writer()
            .writeByte(LPSv3Tags.S_ACTION_WORD, wordResult.ordinal.toByte())
            .writeString(LPSv3Tags.WORD, city)
            .buildAndFlush(asServer = true)
    }

    override fun sendMessage(message: String) {
        writer()
            .writeString(LPSv3Tags.S_ACTION_MSG, message)
            .writeBool(LPSv3Tags.MSG_OWNER, false)
            .buildAndFlush(asServer = true)
    }

    override fun getPlayerData() = cachedPlayerData

    companion object {
        const val LOCAL_NETWORK_IP = "192.168.43.1"
        const val LOCAL_PORT = 8988
    }
}