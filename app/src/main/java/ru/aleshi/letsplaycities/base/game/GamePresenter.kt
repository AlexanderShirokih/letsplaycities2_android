package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.dictionary.DictionaryService
import ru.aleshi.letsplaycities.base.dictionary.DictionaryServiceImpl
import ru.aleshi.letsplaycities.base.dictionary.DictionaryUpdater
import ru.aleshi.letsplaycities.base.dictionary.ExclusionsService
import ru.aleshi.letsplaycities.base.player.UserIdentity
import ru.aleshi.letsplaycities.base.scoring.ScoreManager
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.base.server.ResultWithMessage
import ru.aleshi.letsplaycities.ui.game.CityStatus
import ru.aleshi.letsplaycities.utils.StringUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Presenter which is responsible for controlling game logic.
 * @param dictionary game [DictionaryServiceImpl]
 * @param prefs default [GamePreferences]
 * @param comboSystemView the view implementations of [ComboSystemView]
 * which is used to show users combos info. Now we support only single view for player.
 */
//TODO: Test comboSystemView binding with two players
class GamePresenter @Inject constructor(
    private val dictionary: Single<DictionaryService>,
    private val exclusions: Single<ExclusionsService>,
    private val prefs: GamePreferences,
    private val dictionaryUpdater: DictionaryUpdater,
    private val comboSystemView: ComboSystemView,
    private val scoreManager: ScoreManager
) : GameContract.Presenter {

    /**
     * Disposable for clearing all subscribes
     */
    private val disposable = CompositeDisposable()

    /**
     * Linked View-Model instance
     */
    private lateinit var viewModel: GameContract.ViewModel

    /**
     * Reference to loaded dictionary
     */
    private lateinit var _dictionary: DictionaryService

    /**
     * Reference to loaded exclusions
     */
    private lateinit var _exclusions: ExclusionsService

    /**
     * Reference to current session
     */
    private lateinit var session: GameSession

    /**
     * Disposable for managing game timer
     */
    private var gameTimerDisposable: Disposable? = null

    private val disconnectionSubject = PublishSubject.create<FinishEvent>()

    /**
     * Used to start the game. Initializes all data. After all updates view to GameState.Started
     */
    override fun start(viewModel: GameContract.ViewModel, session: GameSession) {
        this.viewModel = viewModel
        this.session = session

        scoreManager.init(session)
        comboSystemView.init()

        disposable += session.server.friendRequestResult.subscribe(
            viewModel::onFriendRequestResult
        ) { viewModel.updateState(GameState.Error(it)) }

        disposable +=
            Completable.fromAction { viewModel.updateState(GameState.CheckingForUpdates) }
                .andThen(checkForUpdates())
                .andThen(Single.just(GameState.LoadingDictionary))
                .doOnSuccess(viewModel::updateState)
                .flatMap { dictionary }
                .doOnSuccess { _dictionary = it; it.difficulty = prefs.getDifficulty().toByte() }
                .flatMap { exclusions }
                .doOnSuccess { _exclusions = it }
                .doOnSuccess {
                    viewModel.updateState(GameState.Started)
                    session.start(comboSystemView, GameFacade(_dictionary, _exclusions, prefs))
                    updateUserViewAndResetTimer(true)
                }
                .flatMapObservable {
                    Observable.merge(
                        makeMoves(),
                        disconnectionSubject,
                        getKicks()
                    )
                }
                .firstElement()
                .map(scoreManager::getWinner)
                .doFinally(::dispose)
                .subscribe({
                    val playerScore = if (session.gameMode == GameMode.MODE_PVP) -1
                    else session.requirePlayer().score
                    viewModel.updateState(GameState.Finish(it, playerScore))
                }, { err ->
                    err.printStackTrace()
                    viewModel.updateState(GameState.Error(err))
                })
    }

    /**
     * Propagates user input to current user.
     * @return [Observable] that emits [WordCheckingResult] or [Observable.empty] if current user
     * not yet defined or can't make move.
     */
    override fun onUserInput(input: String): Observable<WordCheckingResult> =
        session.sendPlayersInput(input)

    override fun onMessage(message: String): Completable =
        session.sendPlayersMessage(message)

    override fun onSurrender() {
        disconnectionSubject.onNext(
            FinishEvent(
                session.requirePlayer(),
                FinishEvent.Reason.Surrender
            )
        )
        disconnectionSubject.onComplete()
    }

    override fun onPlayerHint(): Completable = session.useHintForPlayer()

    override fun getCurrentSession(): GameSession = session

    /**
     * Sends friend request to [userId] over game server.
     * @param userId id of user that we want to add to friends
     */
    override fun sendFriendRequest(userId: Int): Completable =
        session.server.sendFriendRequest(userId)

    /**
     * Sends ban message to [userId] over game server.
     * @param userId id of user that we want to ban
     */
    override fun banUser(userId: Int): Completable = session.server.banUser(userId)

    /**
     * Call [DictionaryUpdater.checkForUpdates] to fetch updates from server.
     * Doesn't work in local games modes, just completes.
     */
    private fun checkForUpdates(): Completable {
        return if (session.isLocal()) Completable.complete()
        else dictionaryUpdater.checkForUpdates()
            .doOnNext { viewModel.updateState(GameState.LoadingUpdate(it.loadingPercent)) }
            .lastElement()
            .ignoreElement()
    }

    /**
     * Called when system view should be disposed.
     */
    override fun dispose() {
        session.server.dispose()
        disposable.clear()
    }

    /**
     * Used to switch from current user to next. If current user wasn't set before,
     * it will set to first user
     */
    private fun updateUserViewAndResetTimer(newGame: Boolean = false) {
        viewModel.switchUser(if (newGame) null else session.prevUser, session.currentUser)
        resetTimer()
    }

    /**
     * Called to begin start turn for current user.
     */
    private fun makeMoves(): Observable<FinishEvent> {
        return session.makeMoveForCurrentUser()
            .doOnSubscribe { scoreManager.moveStarted() }
            .flatMap {
                if (it.isSuccessful())
                    scoreManager.moveEnded(session.prevUser, it.city).andThen(Observable.just(it))
                else Observable.just(it)
            }
            .doOnNext {
                if (it.isSuccessful()) {
                    _dictionary.markUsed(it.city)
                    updateUserViewAndResetTimer()
                }
            }
            .map(::translateWordsResult)
            .mergeWith(session.getIncomingMessages().map(::translateMessageResult))
            .doOnNext(viewModel::putGameEntity)
            .takeUntil { it is GameEntity.CityInfo && it.status == CityStatus.OK }
            .repeat()
            .ignoreElements()
            .andThen(Observable.empty<FinishEvent>())
            .onErrorResumeNext { t: Throwable ->
                if (t is SurrenderException)
                    Observable.just(
                        if (t.byDisconnection) FinishEvent(
                            t.target,
                            FinishEvent.Reason.Disconnected
                        )
                        else FinishEvent(t.target, FinishEvent.Reason.Surrender)
                    )
                else Observable.error(t)
            }
    }

    /**
     * Translates word result to [GameEntity.CityInfo]
     * @param result input result
     * @return newly created [GameEntity] from [result]
     */
    private fun translateWordsResult(result: ResultWithCity): GameEntity {
        val identityProvider = fun(identity: UserIdentity) =
            session.findUserByIdentity(identity)?.position ?: Position.UNKNOWN
        return GameEntity.CityInfo(result, _dictionary::getCountryCode, identityProvider)
    }

    /**
     * Translates message to [GameEntity.MessageInfo]
     * @param result input result
     * @return newly created [GameEntity] from [result]
     */
    private fun translateMessageResult(result: ResultWithMessage): GameEntity {
        return GameEntity.MessageInfo(result, fun(identity: UserIdentity) =
            session.findUserByIdentity(identity)?.position ?: Position.UNKNOWN)
    }

    private fun getKicks(): Observable<FinishEvent> =
        session.server.kick.map { bySystem ->
            /* Kicked message doesn't provide enough information about who sends the ban
               message, so we do next trick: if player banned by system we will send player
               instance, in another case we will any other user
             */
            FinishEvent(
                if (bySystem)
                    session.requirePlayer()
                else session.users.first { it !== session.requirePlayer() },
                FinishEvent.Reason.Kicked
            )
        }.toObservable()

    /**
     * Used to restart game timer to the time limit defined in the server.
     * If server time limit is `0`, timer won't be started.
     * Called when game starts and every time before move begins.
     */
    private fun resetTimer() {
        gameTimerDisposable?.apply { dispose(); disposable.delete(this) }
        gameTimerDisposable = session.server.getTimer()
            .subscribe({ time ->
                viewModel.updateTime(StringUtils.timeFormat(TimeUnit.SECONDS.toMillis(time)))
            }, {
                //Errors should never happen in this place
            }, {
                disconnectionSubject.onNext(
                    FinishEvent(
                        session.currentUser,
                        FinishEvent.Reason.TimeOut
                    )
                )
                disconnectionSubject.onComplete()
            }).apply { addTo(disposable) }
    }

}