package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.aleshi.letsplaycities.base.player.*
import ru.aleshi.letsplaycities.base.scoring.ScoreManager
import ru.aleshi.letsplaycities.utils.Utils

class GameSession(val players: Array<User>, private val server: BaseServer) : GameContract.Presenter {

    private var mCurrentPlayerIndex: Int = -1
    private var mFirstChar: Char? = null
    private lateinit var mScoreManager: ScoreManager

    lateinit var mExclusions: Exclusions

    lateinit var dictionary: Dictionary
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
        for (user in players) {
            user.gameSession = this
        }
        val context = view.context()
        mScoreManager = ScoreManager(this, findGameMode(), context)
        disposable.add(
            Exclusions.load(context)
                .doOnSuccess { mExclusions = it }
                .flatMap { Dictionary.load(context, mExclusions) }
                .doOnSuccess { dictionary = it }
                .subscribe())

        applyToFragment()
        beginNextMove(null)
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
            Completable.fromAction { view.putCity(city, dictionary.getCountryCode(city), isLeft(player)) }
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
        city?.let { mFirstChar = Utils.findLastSuitableChar(it) }
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
        mExclusions.dispose()
        dictionary.dispose()
    }

    override fun useHint() {
        getPlayer()?.let {
            disposable.add(dictionary.getRandomWord(mFirstChar ?: "абвгдеклмн".random(), true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    getPlayer()?.run { submit(it) {} }
                })
        }
    }

    override fun onSurrender() {
        val res = mScoreManager.getWinner(timeIsUp = false, remote = false)
        view.showGameResults(res)
    }


    private fun getPlayer(): Player? {
        return if (currentPlayer is Player)
            currentPlayer as Player
        else null
    }
}