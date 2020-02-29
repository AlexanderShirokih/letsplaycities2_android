package ru.aleshi.letsplaycities.remote.internal

import io.reactivex.Single
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import ru.aleshi.letsplaycities.BuildConfig
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.AuthType
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo
import ru.quandastudio.lpsclient.model.WordResult

class TestLPSServer {

    private fun createPlayerData(): PlayerData =
            PlayerData.SimpleFactory().create(
                "TestData",
                VersionInfo(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
            )

    private lateinit var lpsServer: LPSServer
    private lateinit var connection: TestConnection
    lateinit var connectionListener: LPSServer.ConnectionListener

    @Before
    fun setUp() {
        connection = TestConnection()
        lpsServer = LPSServerImpl(createPlayerData(), connection, connection.pipe())
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
            .write(
                LPSClientMessage.LPSLogIn(
                    pd = createPlayerData(),
                    fbToken = ""
                )
            )

        verify(connectionListener, timeout(2000).times(1))
            .onMessage(com.nhaarman.mockitokotlin2.any())

        val loginMsg = connection.reader()
        assertTrue(loginMsg is LPSMessage.LPSLoggedIn)

        loginMsg as LPSMessage.LPSLoggedIn

        assertEquals(loginMsg.newerBuild, 1)

        connection
            .write(
                LPSClientMessage.LPSPlay(
                    mode = LPSClientMessage.PlayMode.RANDOM_PAIR,
                    oppUid = null
                )
            )

        val playerData = lpsServer.getPlayerData()

        val playMsg = connection.reader()
        playMsg as LPSMessage.LPSPlayMessage

        assertFalse(playMsg.youStarter)
        assertEquals(playMsg.canReceiveMessages, playerData.canReceiveMessages)
        assertEquals(playMsg.login, playerData.authData.login)
        assertEquals(playMsg.clientVersion, playerData.versionInfo.versionName)
        assertEquals(playMsg.clientBuild, playerData.versionInfo.versionCode)
        assertEquals(playMsg.isFriend, true)
        assertEquals(playMsg.oppUid, playerData.authData.credentials.userId)
        assertEquals(playMsg.authType, playerData.authData.snType)
    }

    @Test
    fun testSendCity() {
        testLogIn()

        connection.write(LPSClientMessage.LPSWord("word"))

        verify(connectionListener, timeout(500).times(1))
            .onMessage(com.nhaarman.mockitokotlin2.any())
    }

    @Test
    fun testSendMsg() {
        testLogIn()

        connection.write(
            LPSClientMessage.LPSMsg("word")
        )

        verify(connectionListener, timeout(500).times(1))
            .onMessage(com.nhaarman.mockitokotlin2.any())
    }

    @Test
    fun testReceiveWord() {
        testLogIn()

        lpsServer.sendCity(WordResult.ACCEPTED, "city", 10)

        val cityMsg = connection.reader()

        cityMsg as LPSMessage.LPSWordMessage

        assertEquals(cityMsg.result, WordResult.ACCEPTED)
        assertEquals(cityMsg.ownerId, 10)
        assertEquals(cityMsg.word, "city")
    }

    @Test
    fun testReceiveMessage() {
        testLogIn()

        lpsServer.sendMessage("Test", 10)

        val cityMsg = connection.reader()

        cityMsg as LPSMessage.LPSMsgMessage

        assertEquals(cityMsg.msg, "Test")
        assertEquals(cityMsg.ownerId, 10)
        assertFalse(cityMsg.isSystemMsg)
    }
}