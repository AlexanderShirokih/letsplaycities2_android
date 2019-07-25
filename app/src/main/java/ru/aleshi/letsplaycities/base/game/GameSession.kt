package ru.aleshi.letsplaycities.base.game

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.player.*
import ru.aleshi.letsplaycities.base.scoring.ScoreManager
import ru.aleshi.letsplaycities.network.lpsv3.LPSMessage
import ru.aleshi.letsplaycities.ui.game.DictionaryUpdater
import ru.aleshi.letsplaycities.utils.StringUtils
import java.util.concurrent.TimeUnit

class GameSession(val players: Array<User>, private val mServer: BaseServer) : GameContract.Presenter {

    private var mCurrentPlayerIndex: Int = -1
    private var mFirstChar: Char? = null
    private val mGameTimer = Observable.interval(0, 1, TimeUnit.SECONDS)
    private var mGameTimerDisposable: Disposable? = null
    private lateinit var mScoreManager: ScoreManager
    private var mDictionary: Dictionary? = null
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
        get() = players[(mCurrentPlayerIndex + 1) % players.size]

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
        mDictionary?.reset()

        for (user in players) {
            user.gameSession = this
            user.reset()
        }

        disposable.addAll(
            mServer.getWordsResult()
                .subscribe({ handleWordResult(it.first, it.second) }, { view::showError })
            , mServer.getInputMessages().observeOn(AndroidSchedulers.mainThread()).subscribe(
                ::postMessage,
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
                        DictionaryUpdater.checkForUpdates(prefs, dic, view.downloadingListener())
                            ?.run { disposable.add(this) }
                    })
        }
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
            msg = gameMode == GameMode.MODE_NET && isMessagesAllowed()
        )
        // Apply to the left
        applyUserView(players[0], false)
        // Apply to the right
        applyUserView(players[1], true)
    }

    private fun isMessagesAllowed(): Boolean {
        return players.all { it.isMessagesAllowed() }
    }

    private fun applyUserView(users: User, isLeft: Boolean) {
        updateLabel(users, isLeft)
        disposable.add(users.getAvatar(view.context())
            .subscribe { view.updateAvatar(it, isLeft) })
    }

    private fun updateLabel(user: User, isLeft: Boolean) {
        view.updateLabel(user.info, isLeft)
    }

    fun onCitySended(city: String, player: User) {
        if (player == currentPlayer) {
            dispatchCityResult(city, player, false)
            mServer.broadcastResult(city)
        }
    }

    private fun isLeft(player: User): Boolean = players.indexOf(player) == 0

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
            if (player != null) view.putCity(city, mDictionary!!.getCountryCode(city), isLeft(player))
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
        view.onHighlightUser(!isLeft(currentPlayer))
        next.onBeginMove(mFirstChar)
        mScoreManager.moveStarted()
    }

    private fun endMove(city: String) {
        mScoreManager.moveEnded(city)
        updateLabel(currentPlayer, !isLeft(currentPlayer))
    }

    fun notify(msg: String) {
        view.showInfo(msg)
    }

    override fun onStop() {
        stopTimer()
        dispose()
    }

    private fun dispose() {
        try {
            throw Exception()
        }catch (e : java.lang.Exception){
            e.printStackTrace()
        }

        disposable.clear()
        mServer.dispose()
        mScoreManager.updateScore()
    }

    override fun onDetachView() {
        mExclusions.dispose()
        mDictionary?.dispose()
    }

    override fun useHint() {
        getPlayer()?.let {
            disposable.add(mDictionary!!.getRandomWord(mFirstChar ?: "абвгдеклмн".random(), true)
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    getPlayer()?.run { submit(it) {} }
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
            getPlayer()?.run {
                submit(word) {}
            }
        } else
            notify(errorMsg!!)
    }

    private fun postMessage(message: String) {
        view.putMessage(message, !isLeft(currentPlayer))
    }

    override fun correct(word: String, errorMsg: String) {
        val prefs = view.getGamePreferences()
        if (prefs.isCorrectionEnabled())
            view.showCorrectionDialog(word, errorMsg)
        else
            postCorrectedWord(null, errorMsg)
    }

    override fun sendMessage(message: String) {
        view.putMessage(message, isLeft(currentPlayer))
        mServer.broadcastMessage(message)
    }

    private fun getPlayer(): Player? {
        return if (currentPlayer is Player)
            currentPlayer as Player
        else null
    }

    private fun getOpp(): User {
        return players.first { it !is Player }
    }

    override fun needsShowMenu(isLeft: Boolean) {
        players.firstOrNull { !isLeft(it) == isLeft }?.run {
            if (needsShowMenu())
                view.showUserMenu(playerData.isFriend, playerData.userName!!, playerData.authData!!.userID)
        }
    }

    override fun sendFriendRequest() {
        mServer.sendFriendRequest()
    }

    override fun dictionary(): Dictionary = mDictionary!!
}