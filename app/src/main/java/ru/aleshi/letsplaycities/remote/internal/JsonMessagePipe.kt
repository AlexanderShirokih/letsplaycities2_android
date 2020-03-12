package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.JsonMessage
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Pipeline for converting incoming and outgoing messages to JSON format
 * @param jsonMessage converter that can transform JSON to LPSMessage
 */
class JsonMessagePipe @Inject constructor(private val jsonMessage: JsonMessage) : MessagePipe {

    /**
     * Sends message to [outputStream] by specific way:
     * Writes `size:{data_size_in_bytes}{\n} and then data previously converted to JSON.
     * @param outputStream stream for writing message
     * @param msg message that will be written to the stream
     */
    override fun write(outputStream: OutputStream, msg: LPSMessage) {
        val data = jsonMessage.write(msg)
        outputStream.bufferedWriter().apply {
            write("size:${data.size}\n")
            write(data)
            flush()
        }
    }

    /**
     * Reads message from [inputStream] and convert it from JSON format to [LPSClientMessage]
     * @param inputStream stream from what messages will be read
     * @return decoded message or `null` if cannot read or deserialize message.
     */
    override fun read(inputStream: InputStream): LPSClientMessage? {
        val reader = JsonMessageReader(inputStream.bufferedReader())
        val data = reader.read()
        return jsonMessage.readClientMessage(data)
    }

}