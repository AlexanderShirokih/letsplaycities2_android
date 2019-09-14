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
import ru.aleshi.letsplaycities.remote.internal.LPSServerMessage
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
        remoteServer.broadcastResult("Word")

        val test = remoteServer.getWordsResult().test()

        test.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        test.assertNoErrors()
            .assertValueCount(1)
            .assertValue { it == WordResult.ACCEPTED to "Word" }
    }

    @Test
    fun getInputMessages() {
        repository.onMessage(LPSServerMessage.LPSMsgServerMessage("test"))

        val test = remoteServer.getInputMessages().test()

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
        remoteServer.broadcastResult("test")

        verify(server, times(1)).sendCity(WordResult.RECEIVED, "test")
    }

    @Test
    fun getTimeLimit() {
        Assert.assertEquals(remoteServer.getTimeLimit(), 92L)
    }
}