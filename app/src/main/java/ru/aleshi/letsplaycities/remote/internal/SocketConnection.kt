package ru.aleshi.letsplaycities.remote.internal

import java.io.*
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject

/**
 * Implementation of [Connection] on socket
 */
class SocketConnection @Inject constructor() : Connection {

    /**
     * Defines buffer size for input and output buffered streams
     */
    private val bufferSize = 64 * 1024

    /**
     * [ServerSocket] is a hosts socket
     */
    private var serverSocket: ServerSocket? = null

    /**
     * [Socket] of a connected client
     */
    private var clientSocket: Socket? = null
    /**
     * Input stream for reading data from client
     */
    private var input: DataInputStream? = null

    /**
     * Output stream fro sending data to client
     */
    private var output: DataOutputStream? = null

    /**
     * Runs socket connection on specified port and waits until client connected
     * @param port port to bind the server
     */
    override fun connect(port: Int) {
        serverSocket = ServerSocket(port)

        // Wait for client to be connected
        clientSocket = serverSocket!!.accept().apply {
            input = DataInputStream(BufferedInputStream(getInputStream(), bufferSize))
            output = DataOutputStream(BufferedOutputStream(getOutputStream(), bufferSize))
        }
    }

    /**
     * Closes server and client connection
     */
    override fun close() {
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            clientSocket?.close()
            clientSocket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            input?.close()
            input = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            output?.close()
            output = null
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * Returns `true` is server started and
     */
    override fun isConnected(): Boolean = serverSocket != null && !serverSocket!!.isClosed


    /**
     * Returns `true` is server has connected clients
     */
    override fun isClientConnected(): Boolean =
        clientSocket != null && clientSocket!!.isConnected && !clientSocket!!.isClosed

    /**
     * Gets input stream for reading data from client
     */
    override fun getInputStream(): DataInputStream = input!!

    /**
     * Gets output stream for sending data to client
     */
    override fun getOutputStream(): DataOutputStream = output!!

}