package ru.aleshi.letsplaycities.remote.internal

import java.io.DataInputStream
import java.io.DataOutputStream

interface Connection {

    fun connect(port: Int)

    fun close()

    fun isConnected(): Boolean

    fun getInputStream(): DataInputStream

    fun getOutputStream(): DataOutputStream
}