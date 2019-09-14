package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult

interface LPSServer {
    interface ConnectionListener {
        fun onMessage(msg: LPSServerMessage)

        fun onProtocolError(err: LPSProtocolError)

        fun onDisconnected()
    }

    fun startServer()

    fun setListener(listener: ConnectionListener)

    fun getListener() : ConnectionListener

    fun close()

    fun sendCity(wordResult: WordResult, city: String)

    fun getPlayerData(): PlayerData
}