package ru.aleshi.letsplaycities.remote.internal

import org.junit.Assert
import org.junit.Test
import java.io.StringReader

class JsonMessageReaderTest {

    @Test
    fun testReadMessage() {
        val input = """
            { { { [{
            "input":"test"
            }] } } }
        """
        val out = JsonMessageReader(StringReader(input)).read()

        Assert.assertArrayEquals(input.toCharArray(), out)
    }
}