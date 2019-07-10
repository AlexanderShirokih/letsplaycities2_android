package ru.aleshi.letsplaycities.network.lpsv3

import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.*

internal sealed class LPSTag(private val tag: Byte) {

    open fun write(dos: DataOutputStream) {
        dos.writeByte(tag.toInt())
    }

    data class LPSBoolTag(val tag: Byte, val value: Boolean) : LPSTag(tag) {

        override fun write(dos: DataOutputStream) {
            super.write(dos)
            dos.writeChar(1)
            dos.writeByte(if (value) 1 else 0)
        }
    }

    data class LPSByteTag(val tag: Byte, val value: Byte) : LPSTag(tag) {

        override fun write(dos: DataOutputStream) {
            super.write(dos)
            dos.writeChar(1)
            dos.writeByte(value.toInt())
        }
    }

    data class LPSCharTag(val tag: Byte, val value: Int) : LPSTag(tag) {

        override fun write(dos: DataOutputStream) {
            super.write(dos)
            dos.writeChar(2)
            dos.writeChar(value)
        }
    }

    data class LPSIntTag(val tag: Byte, val value: Int) : LPSTag(tag) {

        override fun write(dos: DataOutputStream) {
            super.write(dos)
            dos.writeChar(4)
            dos.writeInt(value)
        }
    }

    data class LPSLongTag(val tag: Byte, val value: Long) : LPSTag(tag) {

        override fun write(dos: DataOutputStream) {
            super.write(dos)
            dos.writeChar(8)
            dos.writeLong(value)
        }
    }

    data class LPSUUIDTag(val tag: Byte, val value: UUID) : LPSTag(tag) {

        override fun write(dos: DataOutputStream) {
            super.write(dos)
            dos.writeChar(16)
            dos.writeLong(value.mostSignificantBits)
            dos.writeLong(value.leastSignificantBits)
        }
    }

    data class LPSStringTag(val tag: Byte, val value: String) : LPSTag(tag) {

        override fun write(dos: DataOutputStream) {
            super.write(dos)
            val strData = value.toByteArray()
            dos.writeChar(strData.size)
            dos.write(strData)
        }
    }

    data class LPSBinTag(val tag: Byte, val value: ByteArray) : LPSTag(tag) {

        fun asBoolTag() = LPSBoolTag(tag, value[0] != 0.toByte())

        fun asByteTag() = LPSByteTag(tag, value[0])

        fun asCharTag() = LPSCharTag(tag, ByteBuffer.wrap(value).char.toInt())

        fun asIntTag() = LPSIntTag(tag, ByteBuffer.wrap(value).int)

        fun asLongTag() = LPSLongTag(tag, ByteBuffer.wrap(value).long)

        fun asUUIDTag(): LPSUUIDTag {
            val bb = ByteBuffer.wrap(value)
            return LPSUUIDTag(tag, UUID(bb.long, bb.long))
        }

        fun asStringTag() = LPSStringTag(tag, String(value))

        override fun write(dos: DataOutputStream) {
            super.write(dos)
            dos.writeChar(value.size)
            dos.write(value)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LPSBinTag

            if (tag != other.tag) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = tag.toInt()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }


}