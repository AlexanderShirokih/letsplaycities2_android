package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import java.io.InputStream
import java.io.OutputStream

/**
 * Pipeline for converting incoming and outgoing messages
 */
interface MessagePipe {

    /**
     * Sends message to [outputStream]
     * @param outputStream stream for writing message
     * @param msg message that will be written to the stream
     */
    fun write(outputStream: OutputStream, msg: LPSMessage)

    /**
     * Reads message from [inputStream] and convert it to [LPSClientMessage]
     * @param inputStream stream from what messages will be read
     * @return decoded message or `null` if cannot read or deserialize message.
     */
    fun read(inputStream: InputStream): LPSClientMessage?

}