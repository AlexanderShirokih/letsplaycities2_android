package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult

/**
 * Interface for embedded game server.
 */
interface LPSServer {

    /**
     * Callback interface that listens connection status
     */
    interface ConnectionListener {
        /**
         * Called when input client message was received
         */
        fun onMessage(msg: LPSClientMessage)

        /**
         * Called when something went wrong
         */
        fun onProtocolError(err: LPSProtocolError)

        /**
         * Called when client disconnects from server
         */
        fun onDisconnected()
    }

    /**
     * Starts the server
     */
    fun startServer()

    /**
     * Installs the collection listener
     * @param listener listener to be installed
     */
    fun setListener(listener: ConnectionListener)

    /**
     * Returns current [ConnectionListener]
     */
    fun getListener(): ConnectionListener

    /**
     * Closes server connection
     */
    fun close()

    /**
     * Sends [city] to client
     * @param wordResult result of word validation
     * @param city input city
     * @param ownerId ID of user who sends this city
     */
    fun sendCity(wordResult: WordResult, city: String, ownerId: Int)

    /**
     * Sends [message] to client
     * @param message message
     * @param ownerId ID of user who sends this message
     */
    fun sendMessage(message: String, ownerId: Int)

    /**
     * Returns player data of server owner (who creates the host)
     * @return [PlayerData] of server's owner
     */
    fun getPlayerData(): PlayerData

    /**
     * Returns player data of connected client
     * @return [PlayerData] of connected client or `null` if client not connected yet
     */
    fun getOppData(): PlayerData?
}