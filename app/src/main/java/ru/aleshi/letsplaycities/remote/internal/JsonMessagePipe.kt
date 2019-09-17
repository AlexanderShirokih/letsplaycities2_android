package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.JsonMessage
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class JsonMessagePipe @Inject constructor(private val jsonMessage: JsonMessage) : MessagePipe {

    override fun write(outputStream: OutputStream, msg: LPSMessage) {
        val data = jsonMessage.write(msg)
        outputStream.bufferedWriter().apply {
            write("size:${data.size}\n")
            write(data)
            flush()
        }
    }

    override fun read(inputStream: InputStream): LPSClientMessage {
        // size:[sizeInBytes][\n][data]
        val reader = inputStream.bufferedReader()
        val size = reader.readLine().substring(5).toInt()
        val buffer = CharArray(size)

        reader.read(buffer)
        return jsonMessage.readClientMessage(buffer)
    }

}