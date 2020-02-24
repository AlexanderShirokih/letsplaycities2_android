package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.dictionary.DictionaryService
import ru.aleshi.letsplaycities.base.dictionary.ExclusionsService
import ru.aleshi.letsplaycities.base.player.*
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.base.server.ResultWithMessage
import ru.aleshi.letsplaycities.ui.game.CityStatus
import ru.aleshi.letsplaycities.utils.StringUtils
import ru.quandastudio.lpsclient.model.WordResult

class GameSession(
    val users: Array<User>,
    val server: BaseServer
) {

    /**
     * Facade containing access to dictionary and other game fields.
     */
    lateinit var game: GameFacade
        private set

    /**
     * Returns index of next [User] in array [users]. Index looped in range 0..users.size.
     */
    private val nextIndex: Int
        get() = (_currentUserIndex + 1) % users.size

    /**
     * Last accepted word
     */
    private var lastWord: String = ""

    /**
     * Keeps index of current user
     */
    private var _currentUserIndex = -1

    /**
     * Returns current [User] or `null` if current user not defined yet.
     */
    val currentUser: User?
        get() = if (_currentUserIndex == -1) null else users[_currentUserIndex]

    /**
     * Returns the next [User] in queue
     */
    val nextUser: User
        get() = users[nextIndex]

    /**
     * Call to switch current [User] to next.
     */
    fun switchUsers() {
        _currentUserIndex = nextIndex
    }

    /**
     * Should be called on start to init all [User]s.
     * @param comboSystemView single [ComboSystemView] for [Player]
     * @param dictionary loaded [DictionaryService]
     * @param exclusions loaded [ExclusionsService]
     * @param prefs game preferences instance
     */
    fun start(
        comboSystemView: ComboSystemView,
        dictionary: DictionaryService,
        exclusions: ExclusionsService,
        prefs: GamePreferences,
        gameEntitySupplier: (gameEntity: GameEntity) -> Unit,
        errorHandler: (t: Throwable) -> Unit
    ): Disposable {
        this.game = GameFacade(dictionary, exclusions, prefs)
        users.forEachIndexed { index, user ->
            user.init(
                comboSystemView,
                Position.values()[index],
                game
            )
        }
        return subscribeOnServerEvents(gameEntitySupplier, errorHandler)
    }

    /**
     * Call to start game turn for current user.
     */
    fun beginMove(gameEntitySupplier: (gameEntity: GameEntity) -> Unit): Completable {
        return currentUser!!.onMakeMove(StringUtils.findLastSuitableChar(lastWord))
            .map {
                GameEntity.CityInfo(
                    city = it,
                    position = getUserPosition(currentUser!!),
                    status = CityStatus.WAITING
                )
            }
            .doOnSuccess(gameEntitySupplier)
            .onErrorComplete()
            .flatMapCompletable { cityInfo -> server.sendCity(cityInfo.city, currentUser!!) }
    }

    /**
     * Propagates user input to current user.
     * @return [Observable] that emits [WordCheckingResult] or [Observable.empty] if current user
     * not yet defined or can't make move.
     */
    fun onUserInput(input: String): Observable<WordCheckingResult> =
        currentUser?.run { onUserInput(input) } ?: Observable.empty()

    /**
     * Returns [Position] of [user]
     * @return position of [user]
     */
    private fun getUserPosition(user: User): Position = Position.values()[users.indexOf(user)]

    //NEW CODE


    /**
     * Subscribes on all server events.
     * @param gameEntitySupplier callback to consume [GameEntity] (for city and messages).
     * @return disposable which holds all subscribed events.
     */
    //TODO: Add estimated events
    private fun subscribeOnServerEvents(
        gameEntitySupplier: (gameEntity: GameEntity) -> Unit,
        errorHandler: (t: Throwable) -> Unit
    ): Disposable {
        val disposable = CompositeDisposable()
        disposable.addAll(
            server.getWordsResult()
                .map(::translateWordsResult)
                .subscribe({ res -> gameEntitySupplier(res) }, { errorHandler(it) })
            ,
            server.getIncomingMessages()
                .map(::translateMessages)
                .subscribe({ msgs -> gameEntitySupplier(msgs) }, { errorHandler(it) })
            /*,
            server.leave.subscribe({ leaved ->
                if (leaved) showToastAndDisconnect(R.string.player_leaved, false)
                else showToastAndDisconnect(R.string.opp_disconnect, false)
            }, view::showError, {
                showToastAndDisconnect(R.string.lost_connection, false)
            })
            ,
            server.timeout.subscribe({
                showToastAndDisconnect(R.string.time_out, true)
            }, view::showError) */
        )
        return disposable
    }

    /**
     * Translates word result to [GameEntity.CityInfo]
     * @param result input result
     * @return newly created [GameEntity] from [result]
     */
    private fun translateWordsResult(result: ResultWithCity): GameEntity {
        return when (result.wordResult) {
            WordResult.ACCEPTED, WordResult.RECEIVED -> createCityResult(
                result.city,
                result.identity,
                true
            )
            else -> createCityResult(result.city, result.identity, false)
        }
    }

    /**
     * Creates [GameEntity.CityInfo] from [city]. also fetches country code and user position
     * for [currentUser].
     * @param city input city
     * @param isSuccess if `true` [CityStatus] will be [CityStatus.OK] or [CityStatus.ERROR] otherwise
     * @return new [GameEntity.CityInfo]
     */
    private fun createCityResult(city: String, identity: UserIdentity, isSuccess: Boolean) =
        GameEntity.CityInfo(
            city = city,
            status = if (isSuccess) CityStatus.OK else CityStatus.ERROR,
            countryCode = game.getCountryCode(city),
            position = findUserByIdentity(identity)?.position ?: Position.UNKNOWN
        )

    /**
     * Converts [ResultWithMessage] to [GameEntity.MessageInfo].
     */
    private fun translateMessages(message: ResultWithMessage): GameEntity =
        GameEntity.MessageInfo(
            message = message.message,
            position = findUserByIdentity(message.identity)?.position ?: Position.UNKNOWN
        )

    /**
     * Finds associated user by its [userIdentity].
     * @param userIdentity wanted identity
     * @return [User] associated with [userIdentity] or `null` when its not found.
     */
    private fun findUserByIdentity(userIdentity: UserIdentity): User? =
        users.firstOrNull { userIdentity.isTheSameUser(it) }


    private fun findGameMode(): GameMode {
        return when {
            users.any { it is Android } -> GameMode.MODE_PVA
            users.any { it is RemoteUser } -> GameMode.MODE_MUL
            users.any { it is NetworkUser } -> GameMode.MODE_NET
            else -> GameMode.MODE_PVP
        }
    }

    /**
     * Checks whether current game mode local or not
     * @return `true` if current game mode is PVA or PVP, `false` otherwise.
     */
    fun isLocal(): Boolean {
        val mode = findGameMode()
        return mode == GameMode.MODE_PVA || mode == GameMode.MODE_PVP
    }
/*
    private fun isMessagesAllowed(): Boolean {
        return users.all { it.isMessagesAllowed }
    }

    private fun beginNextMove(city: String) {
        scoreManager.moveStarted()
    }

    private fun endMove(city: String) {
        scoreManager.moveEnded(city)
    }


    private fun dispose() {
        server.dispose()
        scoreManager.updateScore()
    }

    fun useHint() {
        currentUser?.let {
            disposable.add(mDictionary!!.getRandomWord(mFirstChar ?: "абвгдеклмн".random(), true)
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    getCurrentAsPlayer()?.run { onUserInput(it) {} }
                })
        }
    }


    private fun finishGame(timeIsUp: Boolean, remote: Boolean) {
        dispose()
        val res = mScoreManager.getWinner(timeIsUp, remote)
        val score = if (findGameMode() == GameMode.MODE_PVP)
            -1
        else
            users.first { it is Player }.score
        view.showGameResults(res, score)
    }*/

}