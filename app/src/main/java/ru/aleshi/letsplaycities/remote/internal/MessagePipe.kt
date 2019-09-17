package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import java.io.InputStream
import java.io.OutputStream

interface MessagePipe {

    fun write(outputStream: OutputStream, msg: LPSMessage)

    fun read(inputStream: InputStream): LPSClientMessage

}