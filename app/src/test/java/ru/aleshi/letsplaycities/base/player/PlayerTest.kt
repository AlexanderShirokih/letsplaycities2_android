package ru.aleshi.letsplaycities.base.player

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import io.reactivex.Observable
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
import ru.aleshi.letsplaycities.base.game.WordCheckingResult
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.quandastudio.lpsclient.model.*

/**
 * Test for [Player]
 */
@RunWith(MockitoJUnitRunner::class)
class PlayerTest {

    lateinit var server: BaseServer

    @Mock
    lateinit var pictureSource: PictureSource

    lateinit var gameFacade: GameFacade

    lateinit var p: Player

    @Before
    fun setUp() {
        server = Mockito.mock(BaseServer::class.java)

        doAnswer { inv ->
            Observable.just(
                ResultWithCity(
                    wordResult = WordResult.ACCEPTED,
                    city = inv.arguments[0] as String,
                    identity = UserIdIdentity(inv.arguments[1] as User)
                )
            )
        }.`when`(server).sendCity(any(), any())

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
            server,
            PlayerData(AuthData("player", AuthType.Native, Credentials()), VersionInfo("0", 0)),
            pictureSource
        ).apply {
            init(ComboSystem.DefaultSystemView, gameFacade)
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
    fun onUserInputWhenWrongLetter() {
        val tester = TestObserver<ResultWithCity>()

        p.onMakeMove('g')
            .subscribe(tester)

        p.onUserInput("noEx")
            .test().await()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.WrongLetter && v.validLetter == 'g' }
            .assertComplete()

        tester.dispose()
    }

    @Test
    fun onMakeMoveWhenHasFirstWordMatches() {
        val tester = TestObserver<ResultWithCity>()

        p.onMakeMove('n')
            .subscribe(tester)

        p.onUserInput("noEx")
            .test()
            .assertValue { v -> v is WordCheckingResult.Accepted }
            .assertComplete()

        tester.assertValueAt(0) { v -> v.wordResult == WordResult.UNKNOWN && v.city == "noex" }
        tester.assertValueAt(1) { v -> v.isSuccessful() && v.city == "noex" }
        tester.assertComplete()
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
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.NotFound }
    }

    @Test
    fun onUserInputWhenCorrectionsAvailable() {
        p.onUserInput("correction").test().await()
            .assertValueCount(1)
            .assertValue { v -> v is WordCheckingResult.Corrections && v.corrections.contains("correctionAvail") }
    }

}