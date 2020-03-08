package ru.aleshi.letsplaycities.remote

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.UserIdIdentity
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.base.server.ResultWithMessage
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.quandastudio.lpsclient.model.*
import java.util.concurrent.TimeUnit

class RemoteServerTest {

    private val fakePlayerData = PlayerData(
        AuthData("login", AuthType.Native, Credentials(10, "hash")),
        VersionInfo("v1.0", 10)
    )

    @Mock
    lateinit var fakePictureSource: PictureSource

    @Mock
    lateinit var server: LPSServer

    @InjectMocks
    lateinit var repository: RemoteRepository

    lateinit var remoteServer: RemoteServer

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        remoteServer = RemoteServer(repository)

        doNothing().`when`(server).sendCity(any(), any(), any())
    }

    @Test
    fun getWordsResult() {
        val test =
            remoteServer.sendCity("Word", Player(remoteServer, fakePlayerData, fakePictureSource))
                .test()

        test.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        test.assertNoErrors()
            .assertValue {
                it == ResultWithCity(
                    WordResult.ACCEPTED,
                    "Word",
                    identity = UserIdIdentity(10)
                )
            }
    }

    @Test
    fun getInputMessages() {
        remoteServer.sendMessage("test", Player(remoteServer, fakePlayerData, fakePictureSource))
            .blockingAwait()

        val test = remoteServer.getIncomingMessages().test()

        test.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        test.assertNoErrors()
            .assertValueCount(1)
            .assertValue { it == ResultWithMessage("test", UserIdIdentity(10)) }
    }

    @Test
    fun dispose() {
        remoteServer.dispose()

        verify(server, times(1)).close()
    }

    @Test
    fun broadcastResult() {
        remoteServer.sendCity("test", Player(remoteServer, fakePlayerData, fakePictureSource))
            .blockingLast()

        verify(server, times(1)).sendCity(WordResult.RECEIVED, "test", 10)
    }

    @Test
    fun broadcastMessage() {
        remoteServer.sendMessage(
            "Hello message",
            Player(remoteServer, fakePlayerData, fakePictureSource)
        )
            .blockingAwait()

        verify(server, times(1)).sendMessage("Hello message", 10)
    }

}