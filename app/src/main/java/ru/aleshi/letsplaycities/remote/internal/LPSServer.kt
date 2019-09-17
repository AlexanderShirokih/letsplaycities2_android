package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult

interface LPSServer {
    interface ConnectionListener {
        fun onMessage(msg: LPSClientMessage)

        fun onProtocolError(err: LPSProtocolError)

        fun onDisconnected()
    }

    fun startServer()

    fun setListener(listener: ConnectionListener)

    fun getListener() : ConnectionListener

    fun close()

    fun sendCity(wordResult: WordResult, city: String)

    fun sendMessage(message: String)

    fun getPlayerData(): PlayerData
}