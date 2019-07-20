package ru.aleshi.letsplaycities.base.game

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.aleshi.letsplaycities.utils.StringUtils
import ru.aleshi.letsplaycities.base.player.*
import ru.aleshi.letsplaycities.base.scoring.ScoreManager
import java.util.concurrent.TimeUnit

class GameSession(val players: Array<User>, private val server: BaseServer) : GameContract.Presenter {

    private var mCurrentPlayerIndex: Int = -1
    private var mFirstChar: Char? = null
    private lateinit var mGameTimer: Disposable
    private lateinit var mScoreManager: ScoreManager
    private lateinit var mDictionary: Dictionary
    lateinit var mExclusions: Exclusions

    lateinit var view: GameContract.View
    val disposable = CompositeDisposable()

    private val switchToNext: User
        get() {
            mCurrentPlayerIndex = (++mCurrentPlayerIndex) % players.size
            return players[mCurrentPlayerIndex]
        }

    val currentPlayer: User
        get() = players[mCurrentPlayerIndex]


    val nextPlayer: User
        get() = players[(++mCurrentPlayerIndex) % players.size]

    override fun onAttachView(view: GameContract.View) {
        this.view = view
        init()
        val context = view.context()
        mScoreManager = ScoreManager(this, findGameMode(), context)
        loadData(context)
        applyToFragment()
        beginNextMove(null)
    }

    private fun init() {
        mCurrentPlayerIndex = -1
        mFirstChar = null
        if (::mDictionary.isInitialized)
            mDictionary.reset()

        for (user in players) {
            user.gameSession = this
            user.reset()
        }

        val timer = view.getGamePreferences().getTimeLimit()
        if (timer > 0) {
            mGameTimer = Observable.interval(1, TimeUnit.SECONDS)
                .takeUntil { i -> i > timer }
                .subscribe ({
                    view.onTimerUpdate(StringUtils.timeFormat(TimeUnit.SECONDS.toMillis(it)))
                }, ::error, {

                })
        }
    }

    private fun loadData(context: Context) {
        if (!::mExclusions.isInitialized || !::mDictionary.isInitialized)
            disposable.add(
                Exclusions.load(context)
                    .doOnSuccess { mExclusions = it }
                    .flatMap { Dictionary.load(context, mExclusions) }
                    .doOnSuccess { mDictionary = it }
                    .subscribe())
    }

    private fun findGameMode(): GameMode {
        return when {
            players.any { it is Android } -> GameMode.MODE_PVA
            players.any { it is RemoteUser } -> GameMode.MODE_MUL
            players.any { it is NetworkUser } -> GameMode.MODE_NET
            else -> GameMode.MODE_PVP
        }
    }

    private fun applyToFragment() {
        // Apply to the left
        applyUserView(players[0], true)
        // Apply to the right
        applyUserView(players[1], false)
    }

    private fun applyUserView(users: User, isLeft: Boolean) {
        updateLabel(users, isLeft)
        disposable.add(users.getAvatar()
            .subscribe { view.updateAvatar(it, isLeft) })
    }

    private fun updateLabel(user: User, isLeft: Boolean) {
        view.updateLabel(user.info, isLeft)
    }

    fun onCitySended(city: String, player: User) {
        if (player == currentPlayer) {
            Completable.fromAction { view.putCity(city, mDictionary.getCountryCode(city), isLeft(player)) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe()

            disposable.add(
                server.broadcastResult(city)
                    .subscribe({ handleWordResult(it, city) }, { view::showError })
            )
        }
    }

    private fun isLeft(player: User): Boolean = players.indexOf(player) == 0

    private fun handleWordResult(result: WordResult, city: String) {
        when (result) {
            WordResult.ACCEPTED, WordResult.RECEIVED -> {
                Completable.fromAction { view.updateCity(city, false) }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe()

                endMove(city)
                beginNextMove(city)
            }
            else -> {
                Completable.fromAction { view.updateCity(city, true) }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            }
        }
    }

    override fun submit(userInput: String, callback: () -> Unit): Boolean {
        if (currentPlayer is Player) {
            (currentPlayer as Player).submit(userInput, callback)
            return true
        }
        return false
    }

    private fun beginNextMove(city: String?) {
        city?.let { mFirstChar = StringUtils.findLastSuitableChar(it) }
        switchToNext.onBeginMove(mFirstChar)
        mScoreManager.moveStarted()
    }

    private fun endMove(city: String) {
        mScoreManager.moveEnded(city)
        updateLabel(currentPlayer, isLeft(currentPlayer))
    }

    fun onLose(player: User) {
        TODO()
    }

    fun notify(msg: String) {
        view.showInfo(msg)
    }

    override fun onDetachView() {
        mScoreManager.updateScore()
        disposable.dispose()
        mGameTimer.dispose()
        mExclusions.dispose()
        mDictionary.dispose()
    }

    override fun useHint() {
        getPlayer()?.let {
            disposable.add(mDictionary.getRandomWord(mFirstChar ?: "абвгдеклмн".random(), true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    getPlayer()?.run { submit(it) {} }
                })
        }
    }

    override fun onSurrender() {
        val res = mScoreManager.getWinner(timeIsUp = false, remote = false)
        val score = if (findGameMode() == GameMode.MODE_PVP)
            -1
        else
            players.first { it is Player }.score
        view.showGameResults(res, score)
    }

    override fun postCorrectedWord(word: String?, errorMsg: String?) {
        if (word != null) {
            getPlayer()?.run {
                submit(word) {}
            }
        } else
            notify(errorMsg!!)
    }

    override fun correct(word: String, errorMsg: String) {
        val prefs = view.getGamePreferences()
        if (prefs.isCorrectionEnabled())
            view.showCorrectionDialog(word, errorMsg)
        else
            postCorrectedWord(null, errorMsg)
    }


    private fun getPlayer(): Player? {
        return if (currentPlayer is Player)
            currentPlayer as Player
        else null
    }

    override fun dictionary(): Dictionary = mDictionary
}