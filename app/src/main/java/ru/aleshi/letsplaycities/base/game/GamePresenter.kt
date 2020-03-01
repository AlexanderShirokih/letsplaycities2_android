package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.plusAssign
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.dictionary.DictionaryService
import ru.aleshi.letsplaycities.base.dictionary.DictionaryServiceImpl
import ru.aleshi.letsplaycities.base.dictionary.DictionaryUpdater
import ru.aleshi.letsplaycities.base.dictionary.ExclusionsService
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
    private val comboSystemView: ComboSystemView
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


    /**
     * Used to start the game. Initializes all data. After all updates view to GameState.Started
     */
    override fun start(viewModel: GameContract.ViewModel, session: GameSession) {
        this.viewModel = viewModel
        disposable +=
            Completable.fromAction { viewModel.updateState(GameState.CheckingForUpdates) }
                .andThen(checkForUpdates())
                .andThen(Single.just(GameState.LoadingDictionary))
                .doOnSuccess(viewModel::updateState)
                .flatMap { dictionary }
                .doOnSuccess { _dictionary = it; it.difficulty = prefs.getDifficulty().toByte() }
                .flatMap { exclusions }
                .doOnSuccess { _exclusions = it }
                .subscribe({
                    viewModel.updateState(GameState.Started)
                    initPlayers()
                }, { err ->
                    viewModel.updateState(GameState.Error(err))
                })
    }

    /**
     * Called internally on start to init all users
     */
    private fun initPlayers() {
        comboSystemView.init()
        disposable += session.start(
            comboSystemView,
            _dictionary,
            _exclusions,
            prefs,
            viewModel::putGameEntity,
            ::onError
        )
        switchUser()
    }

    /**
     * Propagates user input to current user.
     * @return [Observable] that emits [WordCheckingResult] or [Observable.empty] if current user
     * not yet defined or can't make move.
     */
    override fun onUserInput(input: String): Observable<WordCheckingResult> =
        session.onUserInput(input)

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
     * Error handling function. Disposes all subscribes and shows error on UI.
     */
    private fun onError(t: Throwable) {
        dispose()
        viewModel.updateState(GameState.Error(t))
    }

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
        disposable.clear()
    }

    /**
     * Used to switch from current user to next. If current user wasn't set before,
     * it will set to first user
     */
    private fun switchUser() {
        session.switchUsers()
        viewModel.switchUser(session.currentUser, session.nextUser)
        resetTimer()
        doMove()
    }

    /**
     * Called to begin start turn for current user.
     */
    private fun doMove() {
        disposable += session.beginMove(viewModel::putGameEntity).subscribe()
    }

    /**
     * Used to restart game timer to the time limit defined in the server.
     * If server time limit is `0`, timer won't be started.
     * Called when game starts and every time before move begins.
     */
    private fun resetTimer() {
        val timeLimit = session.server.getTimeLimit()
        if (timeLimit > 0) {
            gameTimerDisposable?.apply { dispose(); disposable.delete(this) }
            gameTimerDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(timeLimit + 1)
                .subscribe({ time ->
                    viewModel.updateTime(StringUtils.timeFormat(TimeUnit.SECONDS.toMillis(timeLimit - time)))
                }, {
                    //Errors should never happen in this place
                }, {
                    viewModel.updateState(GameState.Finish(FinishReason.TimeOut))
                }).apply { addTo(disposable) }
        }
    }

}