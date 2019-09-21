package ru.aleshi.letsplaycities.remote.internal

import ru.aleshi.letsplaycities.network.AndroidBase64Provider
import ru.quandastudio.lpsclient.core.Base64Ext
import ru.quandastudio.lpsclient.core.JsonMessage
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class JsonMessagePipe @Inject constructor(private val jsonMessage: JsonMessage) : MessagePipe {

    companion object {
        init {
            Base64Ext.installBase64(AndroidBase64Provider)
        }
    }

    override fun write(outputStream: OutputStream, msg: LPSMessage) {
        val data = jsonMessage.write(msg)
        outputStream.bufferedWriter().apply {
            write("size:${data.size}\n")
            write(data)
            flush()
        }
    }

    override fun read(inputStream: InputStream): LPSClientMessage {
        val reader = JsonMessageReader(inputStream.bufferedReader())
        val data = reader.read()
        return jsonMessage.readClientMessage(data)
    }

}