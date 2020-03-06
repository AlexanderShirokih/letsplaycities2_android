package ru.aleshi.letsplaycities.base.player

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.game.GameFacade
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo

/**
 * Test for [Android]
 */

@RunWith(MockitoJUnitRunner::class)
class AndroidTest {

    @Mock
    lateinit var pictureSource: PictureSource

    private lateinit var gameFacade: GameFacade

    private lateinit var android: Android

    @Before
    fun setUp() {
        gameFacade = mock(GameFacade::class.java)

        whenever(gameFacade.difficulty).thenReturn(1)

        doAnswer { inv ->
            when (inv.arguments[0] as Char) {
                'j' -> Maybe.just("jump")
                else -> Maybe.empty()
            }
        }.`when`(gameFacade).getRandomWord(any())

        android = Android(
            PlayerData.SimpleFactory().create("android", VersionInfo("0", 0)),
            pictureSource
        ).apply {
            init(ComboSystem.DefaultSystemView, gameFacade)
        }
    }

    @Test
    fun onMakeMoveGeneratesRandomWord() {
        android.onMakeMove('j')
            .test()
            .await()
            .assertNoErrors()
            .assertValue { v -> v.startsWith('j') }
            .dispose()
    }

    @Test
    fun onMakeMoveGeneratesWhenNoWord() {
        android.onMakeMove('n')
            .test().await()
            .assertNoErrors()
            .assertComplete()
            .dispose()
    }

    @Test
    fun onMakeMoveOnEstimateMovesGone() {
        android.estimatedMoves = 1

        // Make first move
        android.onMakeMove('j')
            .test()
            .await()
            .assertValueCount(1)
            .assertNoErrors()
            .dispose()

        //And surrender on next
        android.onMakeMove('j')
            .test()
            .await()
            .assertNoValues()
            .assertNoErrors()
            .assertComplete()
            .dispose()
    }
}