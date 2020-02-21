package ru.aleshi.letsplaycities.remote.internal

import java.io.*
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject

class SocketConnection @Inject constructor() : Connection {

    private val bufferSize = 64 * 1024

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null

    override fun connect(port: Int) {
        serverSocket = ServerSocket(port)

        // Wait for client to be connected
        clientSocket = serverSocket!!.accept().apply {
            input = DataInputStream(BufferedInputStream(getInputStream(), bufferSize))
            output = DataOutputStream(BufferedOutputStream(getOutputStream(), bufferSize))
        }
    }

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

    override fun isConnected(): Boolean = serverSocket != null && !serverSocket!!.isClosed

    override fun isClientConnected(): Boolean =
        clientSocket != null && clientSocket!!.isConnected && !clientSocket!!.isClosed

    override fun getInputStream(): DataInputStream = input!!

    override fun getOutputStream(): DataOutputStream = output!!

}