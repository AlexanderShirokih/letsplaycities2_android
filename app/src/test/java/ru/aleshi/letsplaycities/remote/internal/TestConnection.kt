package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.LPSMessageReader
import ru.quandastudio.lpsclient.core.LPSMessageWriter
import java.io.*

class TestConnection : Connection {

    class CopyOutputStream(pipedInputStream: PipedInputStream) : OutputStream() {
        private val pipe = PipedOutputStream(pipedInputStream)
        override fun write(byte: Int) = pipe.write(byte)
    }

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

    private var isConnected = false

    override fun connect(port: Int) {
        isConnected = true
    }

    override fun close() {
        isConnected = false
        getInputStream().close()
        getOutputStream().close()
        internalIS.close()
        internalOS.close()
    }

    override fun isConnected(): Boolean = isConnected

    override fun getInputStream(): DataInputStream = input

    override fun getOutputStream(): DataOutputStream = output

    fun writer(): LPSMessageWriter {
        return LPSMessageWriter(internalOS)
    }

    fun reader(): LPSMessageReader {
        return LPSMessageReader(internalIS)
    }
}