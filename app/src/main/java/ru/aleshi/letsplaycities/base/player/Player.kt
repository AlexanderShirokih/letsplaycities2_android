package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.dictionary.CityResult
import ru.aleshi.letsplaycities.base.game.GameFacade
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.base.game.WordCheckingResult
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.utils.StringUtils
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.*

/**
 * Represents logic of user controlled player.
 * @param playerData [PlayerData] model class that contains info about user
 * @param pictureSource represents users picture
 */
class Player(
    private val server: BaseServer,
    playerData: PlayerData,
    pictureSource: PictureSource
) :
    User(
        playerData,
        pictureSource
    ) {

    /**
     * @param picasso [Picasso] instance
     * @param playerData [PlayerData] model class that contains info about user
     */
    constructor(
        server: BaseServer,
        playerData: PlayerData,
        picasso: Picasso
    ) : this(
        server,
        playerData,
        PictureSource(
            picasso = picasso,
            uri = Utils.getPictureURI(
                playerData.authData.credentials.userId,
                playerData.pictureHash
            )
        )
    )

    /**
     * [PublishSubject] that will emit user input if it completes all checking.
     */
    private val userInputSubject = PublishSubject.create<WordCheckingResult>()

    /**
     * Saved first letter of the last word
     */
    private var mFirstChar: Char = Char.MIN_VALUE

    override fun onInit(comboSystemView: ComboSystemView): ComboSystem =
        ComboSystem(true, comboSystemView)

    override fun onMakeMove(firstChar: Char): Observable<ResultWithCity> {
        mFirstChar = firstChar
        return userInputSubject
            .flatMap { validateOnServer(it) }
            .takeUntil { it.isSuccessful() }
    }

    /**
     * Processes user input
     */
    fun onUserInput(userInput: String): Observable<WordCheckingResult> {
        val input = StringUtils.formatCity(userInput)
        return Observable.just(input)
            .filter { it.isNotEmpty() }
            .filter { mFirstChar == Char.MIN_VALUE || it[0] == mFirstChar }
            .flatMap { checkForExclusions(input, game).switchIfEmpty(checkInDatabase(input, game)) }
            .flatMap { checkForCorrections(it, game) }
            .doOnNext { userInputSubject.onNext(it) }
    }

    /**
     * Checks current [word] for exclusions.
     * @return  empty [Observable] if word has no exclusions or
     * [WordCheckingResult.Exclusion] if it has.
     */
    private fun checkForExclusions(word: String, game: GameFacade): Observable<WordCheckingResult> =
        Observable.just(word).map { it to game.checkForExclusion(it) }
            .flatMap {
                if (it.second.isEmpty())
                    Observable.empty<WordCheckingResult>()
                else Observable.just(WordCheckingResult.Exclusion(it.second))
            }


    /**
     * Internal class which represents state when input city wasn't found in database
     */
    inner class CityNotFoundException(val city: String) : Exception()

    /**
     * Checks current [city] in game database.
     * @return [Observable] of [WordCheckingResult.Accepted] if [city] was found in database and
     * can be used, [WordCheckingResult.NotFound] if [city] was't found in database,
     * [WordCheckingResult.AlreadyUsed] if word already used before.
     */
    private fun checkInDatabase(city: String, game: GameFacade): Observable<WordCheckingResult> {
        var word = city
        return game.checkCity(word)
            .flatMap { result ->
                when (result) {
                    CityResult.CITY_NOT_FOUND -> Single.error<WordCheckingResult>(
                        CityNotFoundException(word)
                    )
                    CityResult.ALREADY_USED -> Single.just(WordCheckingResult.AlreadyUsed(city))
                    else -> Single.just(WordCheckingResult.Accepted(word))
                }
            }
            .retry { c, t ->
                word = StringUtils.replaceWhitespaces(word)
                c < 2 && t is CityNotFoundException
            }
            .onErrorResumeNext { t: Throwable ->
                if (t is CityNotFoundException)
                    Single.just(
                        WordCheckingResult.NotFound(t.city)
                    )
                else
                    Single.error<WordCheckingResult>(t)
            }.toObservable()
    }

    /**
     * If previous result was [WordCheckingResult.NotFound] this function will search corrections
     * and emit [WordCheckingResult.Corrections] if corrections was found or [WordCheckingResult.NotFound]
     * if corrections if not available.
     * For any other states will emit [Observable.empty].
     */
    private fun checkForCorrections(
        currentResult: WordCheckingResult,
        game: GameFacade
    ): Observable<WordCheckingResult> {
        return if (currentResult is WordCheckingResult.NotFound) {
            game.getCorrections(currentResult.word)
                .flatMapObservable {
                    if (it.isEmpty())
                        Observable.just(WordCheckingResult.NotFound(currentResult.word))
                    else
                        Observable.just(WordCheckingResult.Corrections(it))
                }
        } else
            Observable.just(currentResult)
    }

    /**
     * Sends [result] to server if it [WordCheckingResult.Accepted],
     * otherwise returns [Observable.never].
     * Returns Observable that emits [ResultWithCity] and completes.
     */
    private fun validateOnServer(
        result: WordCheckingResult
    ): Observable<ResultWithCity> {
        return if (result is WordCheckingResult.Accepted) {
            Observable.concatArray(
                Observable.just(
                    ResultWithCity(
                        wordResult = WordResult.UNKNOWN,
                        identity = UserIdIdentity(this),
                        city = result.word
                    )
                ),
                server.sendCity(result.word, this)
            )
        } else Observable.never<ResultWithCity>()
    }

    fun useHint(game: GameFacade): Completable =
        Maybe.just(mFirstChar)
            .map {
                if (mFirstChar == Char.MIN_VALUE) StringUtils.generateFirstChar()
                else mFirstChar
            }
            .flatMap(game::getRandomWord)
            .doOnSuccess { userInputSubject.onNext(WordCheckingResult.Accepted(it)) }
            .ignoreElement()
}