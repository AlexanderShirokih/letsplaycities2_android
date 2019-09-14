package ru.aleshi.letsplaycities.remote

import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.aleshi.letsplaycities.remote.internal.LPSServerMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.WordResult
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
        repository.onMessage(LPSServerMessage.LPSWordServerMessage("Hello"))

        val t = repository.words.test()
        t.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        t.assertNoErrors().assertValue { it.word == "Hello" }
    }

    @Test
    fun getMessages() {
        repository.onMessage(LPSServerMessage.LPSMsgServerMessage("Hello"))

        val t = repository.messages.test()
        t.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        t.assertNoErrors().assertValue { it.message == "Hello" }
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
        repository.onMessage(LPSServerMessage.LPSLeaveServerMessage("leave"))

        val t = repository.leave.test()
        t.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        t.assertNoErrors().assertValue { it.message == "leave" }
    }

    @Test
    fun connect() {
        `when`(server.startServer())
            .then {
                repository
                    .onMessage(LPSServerMessage.LPSConnectedMessage(PlayerData.Factory().create("Test")))
            }

        val t = repository.connect().test()
        t.awaitTerminalEvent()
        t.assertValue { it.authData.login == "Test" }

        verify(server, times(1))
            .startServer()
    }

    @Test
    fun sendWord() {
        repository.sendWord(WordResult.ACCEPTED, "word")

        verify(server, times(1))
            .sendCity(WordResult.ACCEPTED, "word")
        verifyNoMoreInteractions(server)
    }

    @Test
    fun sendMessage() {
        repository.sendMessage("Hello world")

        verify(server, times(1))
            .sendMessage("Hello world")
        verifyNoMoreInteractions(server)
    }
}