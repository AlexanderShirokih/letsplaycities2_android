package ru.aleshi.letsplaycities.remote

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.quandastudio.lpsclient.core.LPSClientMessage
import ru.quandastudio.lpsclient.model.WordResult
import java.util.concurrent.TimeUnit

class RemoteServerTest {

    @Mock
    lateinit var server: LPSServer

    @InjectMocks
    lateinit var repository: RemoteRepository

    lateinit var remoteServer: RemoteServer

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        remoteServer = RemoteServer(repository)

        doNothing().`when`(server).sendCity(any(), any())
    }

    @Test
    fun getWordsResult() {
        remoteServer.broadcastResult("Word").blockingAwait()

        val test = remoteServer.getWordsResult().test()

        test.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        test.assertNoErrors()
            .assertValue { it == WordResult.ACCEPTED to "Word" }
    }

    @Test
    fun getInputMessages() {
        repository.onMessage(LPSClientMessage.LPSMsg("test"))

        val test = remoteServer.getIncomingMessages().test()

        test.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        test.assertNoErrors()
            .assertValueCount(1)
            .assertValue { it == "test" }
    }

    @Test
    fun dispose() {
        remoteServer.dispose()

        verify(server, times(1)).close()
    }

    @Test
    fun broadcastResult() {
        remoteServer.broadcastResult("test").blockingAwait()

        verify(server, times(1)).sendCity(WordResult.RECEIVED, "test")
    }

    @Test
    fun broadcastMessage() {
        remoteServer.broadcastMessage("Hello message").blockingAwait()

        verify(server, times(1)).sendMessage("Hello message")
    }

    @Test
    fun getTimeLimit() {
        Assert.assertEquals(remoteServer.getTimeLimit(), 92L)
    }
}