package ru.aleshi.letsplaycities.network.lpsv3

import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class LPSMessageWriter(private val dos: DataOutputStream) {

    private val tags = ArrayList<LPSTag>()

    @Throws(IOException::class)
    private fun writeHead() {
        dos.writeChar(LPSv3Tags.LPS_CLIENT_VALID)
        dos.writeByte(tags.size)
    }

    @Throws(IOException::class)
    fun build() {
        writeHead()

        for (t in tags)
            t.write(dos)

        tags.clear()
    }

    @Throws(IOException::class)
    fun buildAndFlush() {
        build()
        dos.flush()
    }

    fun writeBool(tag: Byte, data: Boolean): LPSMessageWriter {
        tags.add(LPSTag.LPSBoolTag(tag, data))
        return this
    }

    fun writeByte(tag: Byte, data: Byte): LPSMessageWriter {
        tags.add(LPSTag.LPSByteTag(tag, data))
        return this
    }

    fun writeChar(tag: Byte, data: Int): LPSMessageWriter {
        tags.add(LPSTag.LPSCharTag(tag, data))
        return this
    }

    fun writeInt(tag: Byte, data: Int): LPSMessageWriter {
        tags.add(LPSTag.LPSIntTag(tag, data))
        return this
    }

    fun writeLong(tag: Byte, data: Long): LPSMessageWriter {
        tags.add(LPSTag.LPSLongTag(tag, data))
        return this
    }

    fun writeUUID(tag: Byte, data: UUID): LPSMessageWriter {
        tags.add(LPSTag.LPSUUIDTag(tag, data))
        return this
    }

    fun writeString(tag: Byte, data: String): LPSMessageWriter {
        tags.add(LPSTag.LPSStringTag(tag, data))
        return this
    }

    fun writeBytes(tag: Byte, data: ByteArray): LPSMessageWriter {
        tags.add(LPSTag.LPSBinTag(tag, data))
        return this
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("LPSMsg{")
        for (tag in tags) {
            sb.append(tag.toString())
        }
        sb.append("}")
        return sb.toString()
    }
}