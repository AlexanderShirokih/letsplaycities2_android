package ru.aleshi.letsplaycities.base.player

import com.nhaarman.mockitokotlin2.any
import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.game.GameFacade
import ru.quandastudio.lpsclient.model.VersionInfo

/**
 * Test for [Android]
 */
class AndroidTest {

    private lateinit var picasso: Picasso

    private lateinit var gameFacade: GameFacade

    @Before
    fun setUp() {
        picasso = mock(Picasso::class.java)
        gameFacade = mock(GameFacade::class.java)

        Mockito.`when`(gameFacade.getRandomWord('j')).thenReturn(Maybe.just("jump"))

        Mockito.`when`(gameFacade.getRandomWord('n')).thenReturn(Maybe.empty())
    }

    @Test
    fun onMakeMoveGeneratesRandomWord() {
        val android = Android(picasso, "android", VersionInfo("", 0))
        android.init(ComboSystem.DefaultSystemView, gameFacade)
        android.onMakeMove('j')
            .test()
            .assertNoErrors()
            .assertTimeout()
            .assertValue { v -> v.startsWith('j') }
            .dispose()
    }

    @Test
    fun onMakeMoveGeneratesWhenNoWord() {
        val android = Android(picasso, "android", VersionInfo("", 0))
        android.init(ComboSystem.DefaultSystemView, gameFacade)
        android.onMakeMove('n')
            .test()
            .assertNoErrors()
            .assertTimeout()
            .assertComplete()
            .dispose()
    }

    @Test
    fun onMakeMoveOnEstimateMovesGone() {
        val android = mock(Android::class.java)
        //Mock init method, because by default estimated moves initializes is 1
        Mockito.`when`(android.onInit(any()))
            .thenReturn(ComboSystem(false, ComboSystem.DefaultSystemView))

        // Make first move
        android.onMakeMove('j')
            .test()
            .assertNoErrors()
            .assertTimeout()
            .dispose()

        //And surrender on next
        android.onMakeMove('j')
            .test()
            .assertNoErrors()
            .assertComplete()
            .dispose()
    }
}