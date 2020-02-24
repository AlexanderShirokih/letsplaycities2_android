package ru.aleshi.letsplaycities.base.player

import com.nhaarman.mockitokotlin2.any
import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito
import ru.aleshi.letsplaycities.base.dictionary.CityResult
import ru.aleshi.letsplaycities.base.game.GameFacade
import ru.aleshi.letsplaycities.base.game.WordCheckingResult
import ru.quandastudio.lpsclient.model.VersionInfo

/**
 * Test for [Player]
 */
class PlayerTest {

    private lateinit var picasso: Picasso

    private lateinit var gameFacade: GameFacade

    @Before
    fun setUp() {
        picasso = Mockito.mock(Picasso::class.java)
        gameFacade = Mockito.mock(GameFacade::class.java)

        Mockito.`when`(gameFacade.checkForExclusion("withEx")).thenReturn("exclusion")
        Mockito.`when`(gameFacade.checkForExclusion(any())).thenReturn("")
        Mockito.`when`(gameFacade.checkCity("noWord"))
            .thenReturn(Single.just(CityResult.CITY_NOT_FOUND))
        Mockito.`when`(gameFacade.checkCity("already"))
            .thenReturn(Single.just(CityResult.ALREADY_USED))
        Mockito.`when`(gameFacade.checkCity(any())).thenReturn(Single.just(CityResult.OK))
        Mockito.`when`(gameFacade.getCorrections("correction"))
            .thenReturn(Single.just(listOf("correctionAvail")))
        Mockito.`when`(gameFacade.getCorrections(any())).thenReturn(Single.just(emptyList()))
    }

    @Test
    fun onUserInputWhenHasNoExclusion() {
        val p = Player(picasso, "player", VersionInfo("", 0))
        p.onUserInput("noEx").test()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.Accepted }
            .dispose()
    }

    @Test
    fun onMakeMoveWhenHasFirstWordMatches() {
        val p = Player(picasso, "player", VersionInfo("", 0))
        p.onUserInput("noEx").test()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.Accepted }
            .dispose()
        p.onMakeMove('n')
            .test()
            .assertValue("noEx")
            .dispose()
    }

    @Test
    fun onUserInputWhenHasExclusion() {
        val p = Player(picasso, "player", VersionInfo("", 0))
        p.onUserInput("withEx").test()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.Exclusion && v.description == "exclusion" }
            .dispose()
    }

    @Test
    fun onUserInputWhenAlready() {
        val p = Player(picasso, "player", VersionInfo("", 0))
        p.onUserInput("already").test()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.AlreadyUsed }
            .dispose()
    }

    @Test
    fun onUserInputWhenNoWord() {
        val p = Player(picasso, "player", VersionInfo("", 0))
        p.onUserInput("noWord").test()
            .assertValueCount(2)
            .assertValueAt(0) { v -> v is WordCheckingResult.OriginalNotFound }
            .assertValueAt(1) { v -> v is WordCheckingResult.NotFound }
            .dispose()
    }

    @Test
    fun onUserInputWhenCorrectionsAvailable() {
        val p = Player(picasso, "player", VersionInfo("", 0))
        p.onUserInput("correction").test()
            .assertValueCount(2)
            .assertValueAt(0) { v -> v is WordCheckingResult.OriginalNotFound }
            .assertValueAt(1) { v -> v is WordCheckingResult.Corrections && v.corrections.contains("correctionAvail") }
            .dispose()
    }
}