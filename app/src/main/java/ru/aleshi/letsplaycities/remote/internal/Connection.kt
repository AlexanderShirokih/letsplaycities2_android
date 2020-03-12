package ru.aleshi.letsplaycities.remote.internal

import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * Interface that manages connection between client and server.
 */
interface Connection {

    /**
     * Runs connection on specified port
     * @param port port to bind the server
     */
    fun connect(port: Int)

    /**
     * Closes server and client connection
     */
    fun close()

    /**
     * Returns `true` is server started and
     */
    fun isConnected(): Boolean

    /**
     * Returns `true` is server has connected clients
     */
    fun isClientConnected(): Boolean

    /**
     * Gets input stream for reading data from client
     */
    fun getInputStream(): DataInputStream

    /**
     * Gets output stream for sending data to client
     */
    fun getOutputStream(): DataOutputStream
}