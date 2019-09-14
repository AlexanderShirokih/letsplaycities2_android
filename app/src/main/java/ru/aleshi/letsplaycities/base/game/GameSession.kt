package ru.aleshi.letsplaycities.base.game

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.dictionary.Dictionary
import ru.aleshi.letsplaycities.base.dictionary.DictionaryUpdater
import ru.aleshi.letsplaycities.base.player.*
import ru.aleshi.letsplaycities.base.scoring.ScoreManager
import ru.aleshi.letsplaycities.utils.StringUtils
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.model.WordResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GameSession private constructor(
    val players: Array<User>,
    private val mServer: BaseServer,
    private val dictionaryUpdater: DictionaryUpdater
) :
    GameContract.Presenter {

    class GameSessionBuilder @Inject constructor(private val dictionaryUpdater: DictionaryUpdater) {
        private lateinit var server: BaseServer
        private lateinit var players: Array<User>

        fun server(baseServer: BaseServer): GameSessionBuilder {
            server = baseServer
            return this
        }

        fun users(users: Array<User>): GameSessionBuilder {
            players = users
            return this
        }

        fun build(): GameSession {
            return GameSession(players, server, dictionaryUpdater)
        }
    }

    private var mCurrentPlayerIndex: Int = -1
    private var mFirstChar: Char? = null
    private val mGameTimer = Observable.interval(0, 1, TimeUnit.SECONDS)
    private var mGameTimerDisposable: Disposable? = null
    private var mDictionary: Dictionary? = null
    private lateinit var mScoreManager: ScoreManager
    lateinit var mExclusions: Exclusions

    lateinit var view: GameContract.View

    var difficulty: Int = 1

    val disposable = CompositeDisposable()

    private val switchToNext: User
        get() {
            mCurrentPlayerIndex = (++mCurrentPlayerIndex) % players.size
            return players[mCurrentPlayerIndex]
        }

    val currentPlayer: User
        get() = players[mCurrentPlayerIndex]

    val nextPlayer: User
        get() = players[(mCurrentPlayerIndex + 1) % players.size]

    override fun onAttachView(view: GameContract.View) {
        this.view = view
        init()
        val context = view.context()
        mScoreManager =
            ScoreManager(this, findGameMode(), ComboSystem(view.comboSystemView()), context)
        loadData(context)
        applyToFragment()
        beginNextMove(null)
    }

    private fun init() {
        difficulty = view.getGamePreferences().getDifficulty()
        mCurrentPlayerIndex = -1
        mFirstChar = null
        mDictionary?.run { reset(); setDifficulty(this) }

        for (user in players) {
            user.gameSession = this
            user.reset()
        }

        disposable.addAll(
            mServer.getWordsResult()
                .subscribe({ handleWordResult(it.first, it.second) }, { view::showError })
            , mServer.getInputMessages().observeOn(AndroidSchedulers.mainThread()).subscribe(
                ::onInputMessage,
                view::showError
            )

            , mServer.leave.observeOn(AndroidSchedulers.mainThread()).subscribe({ leaved ->
                if (leaved) showToastAndDisconnect(R.string.player_leaved, false)
                else showToastAndDisconnect(R.string.opp_disconnect, false)
            }, view::showError, {
                showToastAndDisconnect(R.string.lost_connection, false)
            })

            , mServer.timeout.observeOn(AndroidSchedulers.mainThread()).subscribe({
                showToastAndDisconnect(R.string.time_out, true)
            }, view::showError)

            , mServer.friendsRequest.observeOn(AndroidSchedulers.mainThread()).subscribe({
                when (it) {
                    LPSMessage.FriendRequest.NEW_REQUEST -> view.showFriendRequestDialog(getOpp().name)
                    LPSMessage.FriendRequest.ACCEPTED -> view.showInfo(
                        view.context().getString(
                            R.string.friends_request_accepted,
                            getOpp().name
                        )
                    )
                    LPSMessage.FriendRequest.DENIED -> view.showInfo(view.context().getString(R.string.friends_request_denied))
                }
            }, view::showError)
            ,
            mServer.kick.observeOn(AndroidSchedulers.mainThread()).subscribe(::onKicked)
        )
    }

    private fun onKicked(isBannedBySystem: Boolean) {
        if (isBannedBySystem)
            view.showInfo(view.context().getString(R.string.kicked_by_system))
        else
            view.showInfo(view.context().getString(R.string.kicked_by_user))
    }

    override fun banUser(userId: Int) = mServer.banUser(userId)

    override fun onFriendRequestResult(isAccepted: Boolean) {
        mServer.sendFriendAcceptance(isAccepted)
    }


    private fun showToastAndDisconnect(stringID: Int, timeUp: Boolean) {
        view.showInfo(view.context().getString(stringID))
        finishGame(timeUp, true)
    }

    private fun loadData(context: Context) {
        if (!::mExclusions.isInitialized || mDictionary != null) {
            val prefs = view.getGamePreferences()
            disposable.add(
                Exclusions.load(context)
                    .doOnSuccess { mExclusions = it }
                    .flatMap { Dictionary.load(context, mExclusions) }
                    .doOnSuccess { mDictionary = it }
                    .subscribe { dic ->
                        setDifficulty(dic)
                        dictionaryUpdater.checkForUpdates(prefs, dic, view.downloadingListener())
                            ?.run { disposable.add(this) }
                    })
        }
    }

    private fun setDifficulty(dictionary: Dictionary) {
        dictionary.difficulty = difficulty.toByte()
    }

    private fun runTimer() {
        val timer = mServer.getTimeLimit()
        if (timer > 0) {
            stopTimer()
            mGameTimerDisposable = mGameTimer
                .take(timer + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.onTimerUpdate(StringUtils.timeFormat(TimeUnit.SECONDS.toMillis(timer - it)))
                }, ::error, {
                    finishGame(timeIsUp = true, remote = false)
                })
        }
    }

    private fun stopTimer() {
        mGameTimerDisposable?.run { dispose() }
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
        val gameMode = findGameMode()
        view.setMenuItemsVisibility(
            help = gameMode == GameMode.MODE_PVA,
            msg = (gameMode == GameMode.MODE_NET || gameMode == GameMode.MODE_MUL) && isMessagesAllowed()
        )

        players[0].position = Position.LEFT
        players[1].position = Position.RIGHT
        players.forEach(::applyUserView)
    }

    private fun isMessagesAllowed(): Boolean {
        return players.all { it.isMessagesAllowed() }
    }

    private fun applyUserView(user: User) {
        updateLabel(user, user.position)
        disposable.add(user.getAvatar(view.context())
            .subscribe { view.updateAvatar(it, user.position) })
    }

    private fun updateLabel(user: User, position: Position) {
        view.updateLabel(user.info, position)
    }

    fun onCitySended(city: String, player: User) {
        if (player == currentPlayer) {
            dispatchCityResult(city, player, false)
            mServer.broadcastResult(city)
        }
    }

    private fun handleWordResult(result: WordResult, city: String) {
        when (result) {
            WordResult.ACCEPTED -> {
                dispatchCityResult(city, null, false)
                endMove(city)
                beginNextMove(city)
            }
            WordResult.RECEIVED -> {
                dispatchCityResult(city, currentPlayer, false)
                dispatchCityResult(city, null, false)
                endMove(city)
                beginNextMove(city)
            }
            else -> dispatchCityResult(city, null, true)
        }
    }

    private fun dispatchCityResult(city: String, player: User?, hasErrors: Boolean) {
        Completable.fromAction {
            if (player != null) view.putCity(
                city,
                mDictionary!!.getCountryCode(city),
                player.position
            )
            else {
                view.updateCity(city, hasErrors)
                mDictionary!!.applyCity(city)
            }
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun submit(userInput: String, callback: () -> Unit): Boolean {
        if (currentPlayer is Player) {
            (currentPlayer as Player).submit(userInput, callback)
            return true
        }
        return false
    }

    private fun beginNextMove(city: String?) {
        runTimer()
        city?.let { mFirstChar = StringUtils.findLastSuitableChar(it) }
        val next = switchToNext
        view.onHighlightUser(currentPlayer.position)
        next.onBeginMove(mFirstChar)
        mScoreManager.moveStarted()
    }

    private fun endMove(city: String) {
        mScoreManager.moveEnded(city)
        updateLabel(currentPlayer, currentPlayer.position)
    }

    fun notify(msg: String) {
        view.showInfo(msg)
    }

    override fun onStop() {
        stopTimer()
        dispose()
    }

    private fun dispose() {
        disposable.clear()
        mServer.dispose()
        mScoreManager.updateScore()
    }

    override fun onDetachView() {
        if (::mExclusions.isInitialized)
            mExclusions.dispose()
        mDictionary?.dispose()
    }

    override fun useHint() {
        getCurrentAsPlayer()?.let {
            disposable.add(mDictionary!!.getRandomWord(mFirstChar ?: "абвгдеклмн".random(), true)
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    getCurrentAsPlayer()?.run { submit(it) {} }
                })
        }
    }

    override fun onSurrender() {
        finishGame(timeIsUp = false, remote = false)
    }

    private fun finishGame(timeIsUp: Boolean, remote: Boolean) {
        stopTimer()
        dispose()
        val res = mScoreManager.getWinner(timeIsUp, remote)
        val score = if (findGameMode() == GameMode.MODE_PVP)
            -1
        else
            players.first { it is Player }.score
        view.showGameResults(res, score)
    }

    override fun postCorrectedWord(word: String?, errorMsg: String?) {
        if (word != null) {
            getCurrentAsPlayer()?.run {
                submit(word) {}
            }
        } else
            notify(errorMsg!!)
    }

    private fun onInputMessage(message: String) {
        view.putMessage(message, getOpp().position)
    }

    override fun correct(word: String, errorMsg: String) {
        val prefs = view.getGamePreferences()
        if (prefs.isCorrectionEnabled())
            view.showCorrectionDialog(word, errorMsg)
        else
            postCorrectedWord(null, errorMsg)
    }

    override fun sendMessage(message: String) {
        view.putMessage(message, getPlayer().position)
        mServer.broadcastMessage(message)
    }

    private fun getCurrentAsPlayer(): Player? {
        return if (currentPlayer is Player)
            currentPlayer as Player
        else null
    }

    private fun getPlayer(): Player = players.first { it is Player } as Player

    private fun getOpp(): User {
        return players.first { it !is Player }
    }

    override fun needsShowMenu(position: Position) {
        players.firstOrNull { it.position == position }?.run {
            if (needsShowMenu())
                view.showUserMenu(
                    playerData.isFriend,
                    playerData.authData.login,
                    playerData.authData.userID
                )
        }
    }

    override fun sendFriendRequest() {
        mServer.sendFriendRequest()
    }

    override fun dictionary(): Dictionary = mDictionary!!
}