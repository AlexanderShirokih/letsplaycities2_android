package ru.aleshi.letsplaycities.remote.internal

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import ru.quandastudio.lpsclient.core.LPSv3Tags
import ru.quandastudio.lpsclient.model.AuthType
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult

class TestLPSServer {

    private fun createPlayerData(): PlayerData {
        return PlayerData.Factory().create("TestData")
    }

    private val playerData = createPlayerData()
    private lateinit var lpsServer: LPSServer
    private lateinit var connection: TestConnection
    lateinit var connectionListener: LPSServer.ConnectionListener

    @Before
    fun setUp() {
        connection = TestConnection()
        lpsServer = LPSServerImpl(playerData, connection)
        connectionListener = mock(LPSServer.ConnectionListener::class.java)
        lpsServer.setListener(connectionListener)
        lpsServer.startServer()
    }

    @After
    fun tearDown() {
        lpsServer.close()
    }


    @Test
    fun testLogIn() {
        connection
            .writer()
            .writeByte(LPSv3Tags.ACTION_LOGIN, 4)
            .writeString(LPSv3Tags.LOGIN, "TestClient")
            .writeString(LPSv3Tags.SN_UID, "test-snuid")
            .writeByte(LPSv3Tags.SN, AuthType.Native.ordinal.toByte())
            .writeBool(LPSv3Tags.CAN_REC_MSG, false)
            .writeChar(LPSv3Tags.CLIENT_BUILD, 10)
            .writeString(LPSv3Tags.CLIENT_VERSION, "test")
            .buildAndFlush()

        verify(connectionListener, timeout(2000).times(1))
            .onMessage(com.nhaarman.mockitokotlin2.any())

        val loginMsg = connection.reader()
        assertTrue(loginMsg.readBoolean(LPSv3Tags.ACTION_LOGIN_RESULT))
        assertEquals(loginMsg.readChar(LPSv3Tags.NEWER_BUILD), 1)
        assertEquals(loginMsg.readInt(LPSv3Tags.S_UID), 1)
        assertEquals(loginMsg.readString(LPSv3Tags.S_ACC_HASH), "-remote-")

        connection
            .writer()
            .writeBool(LPSv3Tags.ACTION_PLAY, false)
            .buildAndFlush()

        val playMsg = connection.reader()
        assertFalse(playMsg.readBoolean(LPSv3Tags.ACTION_JOIN))
        assertEquals(playMsg.readBoolean(LPSv3Tags.S_CAN_REC_MSG), playerData.canReceiveMessages)
        assertEquals(playMsg.readString(LPSv3Tags.OPP_LOGIN), playerData.authData.login)
        assertEquals(playMsg.readString(LPSv3Tags.OPP_CLIENT_VERSION), playerData.clientVersion)
        assertEquals(playMsg.readChar(LPSv3Tags.OPP_CLIENT_BUILD), playerData.clientBuild)
        assertEquals(playMsg.readBoolean(LPSv3Tags.OPP_IS_FRIEND), true)
        assertEquals(playMsg.readInt(LPSv3Tags.S_OPP_UID), playerData.authData.userID)
        assertEquals(playMsg.readString(LPSv3Tags.S_OPP_SNUID), playerData.authData.snUID)
        assertEquals(
            playMsg.readByte(LPSv3Tags.S_OPP_SN),
            playerData.authData.snType.ordinal.toByte()
        )
    }

    @Test
    fun testSendCity() {
        testLogIn()

        connection.writer()
            .writeString(LPSv3Tags.ACTION_WORD, "word")
            .buildAndFlush()

        verify(connectionListener, timeout(500).times(1))
            .onMessage(com.nhaarman.mockitokotlin2.any())
    }

    @Test
    fun testSendMsg() {
        testLogIn()

        connection.writer()
            .writeString(LPSv3Tags.ACTION_MSG, "word")
            .buildAndFlush()

        verify(connectionListener, timeout(500).times(1))
            .onMessage(com.nhaarman.mockitokotlin2.any())
    }

    @Test
    fun testReceiveWord() {
        testLogIn()

        lpsServer.sendCity(WordResult.ACCEPTED, "city")

        val cityMsg = connection.reader()
        assertEquals(cityMsg.readByte(LPSv3Tags.S_ACTION_WORD), WordResult.ACCEPTED.ordinal.toByte())
        assertEquals(cityMsg.readString(LPSv3Tags.WORD), "city")
    }
}