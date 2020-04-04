package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
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
    fun onUserInput(userInput: String): Flowable<WordCheckingResult> {
        val input = StringUtils.formatCity(userInput)
        return Flowable.just(input)
            .filter { it.isNotEmpty() }
            .flatMap {
                checkFirstLetterMatches(input).switchIfEmpty(
                    checkForExclusions(input, game).switchIfEmpty(
                        checkInDatabase(
                            input,
                            game
                        )
                    )
                )
            }
            .flatMap { checkForCorrections(it, game) }
            .doOnNext { userInputSubject.onNext(it) }
    }

    /**
     * Checks that [userInput] starts with [mFirstChar]
     */
    private fun checkFirstLetterMatches(userInput: String): Flowable<WordCheckingResult> =
        if (mFirstChar == Char.MIN_VALUE || mFirstChar == userInput[0])
            Flowable.empty()
        else
            Flowable.just(WordCheckingResult.WrongLetter(mFirstChar))

    /**
     * Checks current [word] for exclusions.
     * @return  empty [Observable] if word has no exclusions or
     * [WordCheckingResult.Exclusion] if it has.
     */
    private fun checkForExclusions(word: String, game: GameFacade): Flowable<WordCheckingResult> =
        Flowable.just(word).map { it to game.checkForExclusion(it) }
            .subscribeOn(Schedulers.computation())
            .onBackpressureLatest()
            .flatMap {
                if (it.second.isEmpty())
                    Flowable.empty()
                else Flowable.just(WordCheckingResult.Exclusion(it.second))
            }


    /**
     * Internal class which represents state when input city wasn't found in database
     */
    inner class CityNotFoundException(val city: String) : Exception()

    /**
     * Checks current [city] in game database.
     * @return [Flowable] of [WordCheckingResult.Accepted] if [city] was found in database and
     * can be used, [WordCheckingResult.NotFound] if [city] was't found in database,
     * [WordCheckingResult.AlreadyUsed] if word already used before.
     */
    private fun checkInDatabase(city: String, game: GameFacade): Flowable<WordCheckingResult> {
        var word = city
        return game.checkCity { word }
            .flatMap { result ->
                when (result) {
                    CityResult.CITY_NOT_FOUND -> Flowable.error(
                        CityNotFoundException(word)
                    )
                    CityResult.ALREADY_USED -> Flowable.just(WordCheckingResult.AlreadyUsed(city))
                    else -> Flowable.just(WordCheckingResult.Accepted(word))
                }
            }
            .retry { c, t ->
                word = word.replace(" ", "-")
                c < 2 && t is CityNotFoundException
            }
            .onErrorResumeNext { t: Throwable ->
                if (t is CityNotFoundException)
                    Flowable.just(
                        WordCheckingResult.NotFound(t.city)
                    )
                else
                    Flowable.error(t)
            }
    }

    /**
     * If previous result was [WordCheckingResult.NotFound] this function will search corrections
     * and emit [WordCheckingResult.Corrections] if corrections was found or [WordCheckingResult.NotFound]
     * if corrections not available.
     * For any other states will emit [Flowable.empty].
     */
    private fun checkForCorrections(
        currentResult: WordCheckingResult,
        game: GameFacade
    ): Flowable<WordCheckingResult> {
        return if (currentResult is WordCheckingResult.NotFound && currentResult.word.length > 3) {
            game.getCorrections(currentResult.word)
                .subscribeOn(Schedulers.computation())
                .flatMapPublisher { correctionsList ->
                    Flowable.just(
                        when (correctionsList.size) {
                            0 -> WordCheckingResult.NotFound(currentResult.word)
                            1 -> WordCheckingResult.Accepted(correctionsList[0])
                            else -> WordCheckingResult.Corrections(correctionsList)
                        }
                    )
                }
                .onBackpressureLatest()
        } else
            Flowable.just(currentResult)
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
        } else Observable.never()
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