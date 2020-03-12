package ru.aleshi.letsplaycities.remote

import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.model.*
import java.util.concurrent.TimeUnit

class RemoteRepositoryTest {

    lateinit var repository: RemoteRepository
    lateinit var server: LPSServer

    @Before
    fun setUp() {
        server = mock(LPSServer::class.java)
        repository = RemoteRepository(server)
    }

    @After
    fun tearDown() {
        repository.disconnect()
    }

    @Test
    fun getWords() {
        repository.onMessage(LPSClientMessage.LPSWord("Hello"))

        val t = repository.words.test()
        t.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        t.assertNoErrors().assertValue { it.word == "Hello" }
    }

    @Test
    fun getMessages() {
        repository.onMessage(LPSClientMessage.LPSMsg("Hello"))

        val t = repository.messages.test()
        t.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        t.assertNoErrors().assertValue { it.msg == "Hello" }
    }

    @Test
    fun testDisconnect() {
        repository.onDisconnected()

        val t = repository.messages.test()
        t.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        t.assertNoErrors().assertComplete()
    }

    @Test
    fun getLeave() {
        repository.onMessage(LPSClientMessage.LPSLeave("leave"))

        val t = repository.leave.test()
        t.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        t.assertNoErrors().assertValue { it.reason == "leave" }
    }

    @Test
    fun connect() {
        `when`(server.startServer())
            .then {
                repository
                    .onMessage(
                        LPSClientMessage.LPSLogIn(
                            pd = PlayerData(
                                AuthData(
                                    "Test", AuthType.Native, Credentials()
                                ), VersionInfo(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
                            ),
                            fbToken = ""
                        )
                    )
            }

        val t = repository.connect().test()
        t.awaitTerminalEvent()
        t.assertValue { it.authData.login == "Test" }

        verify(server, times(1))
            .startServer()
    }

    @Test
    fun sendWord() {
        repository.sendWord(WordResult.ACCEPTED, "word", 10)

        verify(server, times(1))
            .sendCity(WordResult.ACCEPTED, "word", 10)
        verifyNoMoreInteractions(server)
    }

    @Test
    fun sendMessage() {
        repository.sendMessage("Hello world", 10)

        verify(server, times(1))
            .sendMessage("Hello world", 10)
        verifyNoMoreInteractions(server)
    }
}