package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.dictionary.CityResult
import ru.aleshi.letsplaycities.base.game.GameFacade
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.base.game.WordCheckingResult
import ru.aleshi.letsplaycities.utils.StringUtils
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo

/**
 * Represents logic of user controlled player.
 * @param playerData [PlayerData] model class that contains info about user
 * @param pictureSource represents users picture
 */
class Player(
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
        playerData: PlayerData,
        picasso: Picasso
    ) : this(
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
     * Constructor used for local games.
     * @param picasso [Picasso] instance
     * @param name localized user names
     * @param versionInfo application [VersionInfo] instance
     */
    constructor(
        picasso: Picasso,
        name: String,
        versionInfo: VersionInfo
    ) : this(
        PlayerData.SimpleFactory().create(name, versionInfo),
        picasso
    )

    /**
     * [PublishSubject] that will emit user input if it completes all checking.
     */
    private val userInputSubject = BehaviorSubject.create<String>()

    /**
     * Saved first letter of the last word
     */
    private var mFirstChar: Char = Char.MIN_VALUE

    override fun onInit(comboSystemView: ComboSystemView): ComboSystem =
        ComboSystem(true, comboSystemView)

    override fun onMakeMove(firstChar: Char): Maybe<String> {
        mFirstChar = firstChar
        return userInputSubject.firstElement()
    }

    override fun onUserInput(userInput: String): Observable<WordCheckingResult> {
        val input = StringUtils.formatCity(userInput)
        return Observable.just(input)
            .filter { it.isNotEmpty() }
            .filter { mFirstChar == Char.MIN_VALUE || it[0] == mFirstChar }
            .flatMap { checkForExclusions(input, game).switchIfEmpty(checkInDatabase(input, game)) }
            .flatMap { checkForCorrections(it, game) }
            .doOnNext {
                if (it is WordCheckingResult.Accepted)
                    userInputSubject.onNext(it.word)
            }
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
     * can be used, [WordCheckingResult.OriginalNotFound] if [city] was't found in database,
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
                    CityResult.ALREADY_USED -> Single.just(WordCheckingResult.AlreadyUsed)
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
                        WordCheckingResult.OriginalNotFound(t.city)
                    )
                else
                    Single.error<WordCheckingResult>(t)
            }.toObservable()
    }

    /**
     * If previous result was [WordCheckingResult.OriginalNotFound] this function will search corrections
     * and emit [WordCheckingResult.Corrections] if corrections was found or [WordCheckingResult.NotFound]
     * if corrections if not available.
     * For any other states will emit [Observable.empty].
     */
    private fun checkForCorrections(
        currentResult: WordCheckingResult,
        game: GameFacade
    ): Observable<WordCheckingResult> =
        if (currentResult is WordCheckingResult.OriginalNotFound) {
            Observable.concatArray(Observable.just(currentResult),
                game.getCorrections(currentResult.word)
                    .flatMapObservable {
                        if (it.isEmpty())
                            Observable.just(WordCheckingResult.NotFound)
                        else
                            Observable.just(WordCheckingResult.Corrections(it))
                    })
        } else
            Observable.just(currentResult)
}