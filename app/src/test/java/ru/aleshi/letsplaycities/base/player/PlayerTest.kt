package ru.aleshi.letsplaycities.base.player

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.dictionary.CityResult
import ru.aleshi.letsplaycities.base.game.GameFacade
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.base.game.WordCheckingResult
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo

/**
 * Test for [Player]
 */
@RunWith(MockitoJUnitRunner::class)
class PlayerTest {

    @Mock
    lateinit var pictureSource: PictureSource

    lateinit var gameFacade: GameFacade

    lateinit var p: Player

    @Before
    fun setUp() {
        gameFacade = Mockito.mock(GameFacade::class.java)

        doAnswer { inv ->
            when (inv.arguments[0] as String) {
                "withex" -> "exclusion"
                else -> ""
            }
        }.`when`(gameFacade).checkForExclusion(any())

        doAnswer { inv ->
            when (inv.arguments[0] as String) {
                "noword", "correction" -> Single.just(CityResult.CITY_NOT_FOUND)
                "already" -> Single.just(CityResult.ALREADY_USED)
                else -> Single.just(CityResult.OK)
            }
        }.`when`(gameFacade).checkCity(any())

        doAnswer { inv ->
            when (inv.arguments[0] as String) {
                "correction" -> Single.just(listOf("correctionAvail"))
                else -> Single.just(emptyList())
            }
        }.`when`(gameFacade).getCorrections(any())

        p = Player(
            PlayerData.SimpleFactory().create("player", VersionInfo("0", 0)),
            pictureSource
        ).apply {
            init(ComboSystem.DefaultSystemView, Position.UNKNOWN, gameFacade)
        }
    }

    @Test
    fun onUserInputWhenHasNoExclusion() {
        p.onUserInput("noEx").test().await()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.Accepted }
            .assertComplete()
    }

    @Test
    fun onUserInputWhenHasExclusion() {
        p.onUserInput("withEx").test().await()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.Exclusion && v.description == "exclusion" }
    }

    @Test
    fun onMakeMoveWhenHasFirstWordMatches() {
        val tester = TestObserver<String>()

        p.onMakeMove('n')
            .subscribe(tester)

        p.onUserInput("noEx")
            .test()
            .assertValue { v -> v is WordCheckingResult.Accepted }
            .assertComplete()

        tester.assertValue("noex")
        tester.dispose()
    }

    @Test
    fun onUserInputWhenAlready() {
        p.onUserInput("already").test().await()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.AlreadyUsed }
    }

    @Test
    fun onUserInputWhenNoWord() {
        p.onUserInput("noword").test().await()
            .assertValueCount(2)
            .assertValueAt(0) { v -> v is WordCheckingResult.OriginalNotFound }
            .assertValueAt(1) { v -> v is WordCheckingResult.NotFound }
    }

    @Test
    fun onUserInputWhenCorrectionsAvailable() {
        p.onUserInput("correction").test().await()
            .assertValueCount(2)
            .assertValueAt(0) { v -> v is WordCheckingResult.OriginalNotFound }
            .assertValueAt(1) { v -> v is WordCheckingResult.Corrections && v.corrections.contains("correctionAvail") }
    }

}