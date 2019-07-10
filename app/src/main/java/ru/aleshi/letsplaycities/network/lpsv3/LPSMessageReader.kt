package ru.aleshi.letsplaycities.network.lpsv3

import java.io.DataInputStream
import java.util.*

class LPSMessageReader(private val dis: DataInputStream) {

    private val tags: Array<LPSTag.LPSBinTag>

    private val numTags: Int
    private var tagCntr: Int = 0

    init {
        val magic = dis.readChar()
        if (magic.toInt() != LPSv3Tags.LPS_SERVER_VALID) {
            throw LPSException("Received an invalid LPS Message!")
        } else {
            numTags = dis.readUnsignedByte()
            tags = Array(numTags) { readTag() }
        }
    }

    private fun readTag(): LPSTag.LPSBinTag {
        val tag = dis.readUnsignedByte()
        if (tag == 0)
            throw LPSException("LPS tag cannot be 0")
        val length = dis.readUnsignedShort()

        val data = ByteArray(length)
        var readed = 0

        while (readed < length) {
            readed += dis.read(data, readed, length - readed)
        }

        return LPSTag.LPSBinTag(tag.toByte(), data)
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("LPSMessageReader [tags=")
        builder.append(Arrays.toString(tags))
        builder.append(", numTags=")
        builder.append(numTags)
        builder.append("]")
        return builder.toString()
    }

    fun nextTag(): Byte {
        return if (tagCntr == numTags) -1 else tags[tagCntr++].tag
    }

    fun getMasterTag(): Byte {
        return tags[0].tag
    }

    private fun getTag(tag: Byte): LPSTag.LPSBinTag {
        return tags.first { it.tag == tag }
    }

    fun hasTag(tag: Byte): Boolean {
        return tags.indexOfFirst { it.tag == tag } > -1
    }

    fun readBoolean(tag: Byte): Boolean {
        return getTag(tag).asBoolTag().value
    }

    fun readByte(tag: Byte): Byte {
        return getTag(tag).asByteTag().value
    }


    fun readChar(tag: Byte): Int {
        return getTag(tag).asCharTag().value
    }

    fun readInt(tag: Byte): Int {
        return getTag(tag).asIntTag().value
    }

    fun readLong(tag: Byte): Long {
        return getTag(tag).asLongTag().value
    }

    fun readString(tag: Byte): String {
        return getTag(tag).asStringTag().value
    }

    fun readBytes(tag: Byte): ByteArray {
        return getTag(tag).value
    }

    fun optBoolean(tag: Byte): Boolean {
        return if (hasTag(tag)) readBoolean(tag) else false
    }

    fun optByte(tag: Byte): Byte? {
        return if (hasTag(tag)) readByte(tag) else 0
    }

    fun optChar(tag: Byte): Int {
        return (if (hasTag(tag)) readChar(tag) else 0)
    }

    fun optInt(tag: Byte): Int {
        return if (hasTag(tag)) readInt(tag) else 0
    }

    fun optLong(tag: Byte): Long {
        return if (hasTag(tag)) readLong(tag) else 0
    }

    fun optString(tag: Byte): String? {
        return if (hasTag(tag)) readString(tag) else null
    }

    fun <T : Enum<T>> optEnum(type: Class<T>, tag: Byte): T? {
        val t = type.enumConstants!!
        return if (hasTag(tag)) t[readByte(tag).toInt()] else null
    }

}