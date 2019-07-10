package ru.aleshi.letsplaycities.network.lpsv3

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.*

class LPSMessageWriterTest {

    @Test
    fun writeBool() {
        val bos = ByteArrayOutputStream()
        LPSMessageWriter(DataOutputStream(bos))
            .writeBool(3, false)
            .writeBool(4, true)
            .buildAndFlush()

        Assert.assertArrayEquals(
            byteArrayOf(0xEB.toByte(), 0xBE.toByte(), 2, 3, 0, 1, 0, 4, 0, 1, 1),
            bos.toByteArray()
        )
    }


    @Test
    fun writeByte() {
        val bos = ByteArrayOutputStream()
        LPSMessageWriter(DataOutputStream(bos))
            .writeByte(3, 14)
            .writeByte(4, 12)
            .buildAndFlush()

        Assert.assertArrayEquals(
            byteArrayOf(0xEB.toByte(), 0xBE.toByte(), 2, 3, 0, 1, 14, 4, 0, 1, 12),
            bos.toByteArray()
        )
    }

    @Test
    fun writeChar() {
        val bos = ByteArrayOutputStream()
        LPSMessageWriter(DataOutputStream(bos))
            .writeChar(3, 14)
            .writeChar(4, 12)
            .buildAndFlush()

        Assert.assertArrayEquals(
            byteArrayOf(0xEB.toByte(), 0xBE.toByte(), 2, 3, 0, 2, 0, 14, 4, 0, 2, 0, 12),
            bos.toByteArray()
        )
    }

    @Test
    fun writeInt() {
        val bos = ByteArrayOutputStream()
        LPSMessageWriter(DataOutputStream(bos))
            .writeInt(3, 14)
            .writeInt(4, 12)
            .buildAndFlush()

        Assert.assertArrayEquals(
            byteArrayOf(0xEB.toByte(), 0xBE.toByte(), 2, 3, 0, 4, 0, 0, 0, 14, 4, 0, 4, 0, 0, 0, 12),
            bos.toByteArray()
        )
    }

    @Test
    fun writeLong() {
        val bos = ByteArrayOutputStream()
        LPSMessageWriter(DataOutputStream(bos))
            .writeLong(3, 14)
            .writeLong(4, 12)
            .buildAndFlush()

        val eb = 0xEB.toByte()
        val be = 0xBE.toByte()
        Assert.assertArrayEquals(
            byteArrayOf(eb, be, 2, 3, 0, 8, 0, 0, 0, 0, 0, 0, 0, 14, 4, 0, 8, 0, 0, 0, 0, 0, 0, 0, 12),
            bos.toByteArray()
        )
    }

    @Test
    fun writeUUID() {
        val bos = ByteArrayOutputStream()
        LPSMessageWriter(DataOutputStream(bos))
            .writeUUID(3, UUID(10, 12))
            .buildAndFlush()

        val eb = 0xEB.toByte()
        val be = 0xBE.toByte()
        Assert.assertArrayEquals(
            byteArrayOf(eb, be, 1, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 12),
            bos.toByteArray()
        )
    }

    @Test
    fun writeString() {
        val bos = ByteArrayOutputStream()
        LPSMessageWriter(DataOutputStream(bos))
            .writeString(3, "Hello")
            .buildAndFlush()

        val eb = 0xEB.toByte()
        val be = 0xBE.toByte()
        Assert.assertArrayEquals(
            byteArrayOf(eb, be, 1, 3, 0, 5, 'H'.toByte(), 'e'.toByte(), 'l'.toByte(), 'l'.toByte(), 'o'.toByte()),
            bos.toByteArray()
        )
    }

    @Test
    fun writeBytes() {
        val bos = ByteArrayOutputStream()
        LPSMessageWriter(DataOutputStream(bos))
            .writeBytes(3, byteArrayOf(4, 3, 2, 1))
            .buildAndFlush()

        val eb = 0xEB.toByte()
        val be = 0xBE.toByte()
        Assert.assertArrayEquals(
            byteArrayOf(eb, be, 1, 3, 0, 4, 4, 3, 2, 1),
            bos.toByteArray()
        )
    }
}