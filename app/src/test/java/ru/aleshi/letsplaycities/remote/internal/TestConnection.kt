package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.JsonMessage
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import java.io.*

class TestConnection : Connection {

    class CopyOutputStream(pipedInputStream: PipedInputStream) : OutputStream() {
        private val pipe = PipedOutputStream(pipedInputStream)
        override fun write(byte: Int) = pipe.write(byte)
    }

    private val messagePipe = JsonMessage()
    private val pipedInput: PipedInputStream = PipedInputStream()
    private val pipedOut: PipedInputStream = PipedInputStream()

    private val input = DataInputStream(pipedInput)
    private val output = DataOutputStream(PipedOutputStream(pipedOut))

    private val internalOS = DataOutputStream(
        CopyOutputStream(
            pipedInput
        )
    )
    private val internalIS = DataInputStream(pipedOut)

    private var connected = false

    override fun connect(port: Int) {
        connected = true
    }

    override fun close() {
        connected = false
        getInputStream().close()
        getOutputStream().close()
        internalIS.close()
        internalOS.close()
    }

    override fun isConnected(): Boolean = connected

    override fun isClientConnected(): Boolean = connected

    override fun getInputStream(): DataInputStream = input

    override fun getOutputStream(): DataOutputStream = output

    fun write(msg: LPSClientMessage) {
        val data = messagePipe.write(msg)
        internalOS.bufferedWriter().use {
            it.write("size:${data.size}\n")
            it.write(data)
        }
    }

    fun reader(): LPSMessage {
        val reader = internalIS.bufferedReader()
        val size = reader.readLine().substring(5).toInt()
        val buffer = CharArray(size)
        reader.read(buffer)
        return messagePipe.readMessage(buffer)
    }

    fun pipe(): MessagePipe = JsonMessagePipe(messagePipe)

}