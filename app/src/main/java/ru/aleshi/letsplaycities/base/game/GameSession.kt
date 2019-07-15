package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.utils.Utils

class GameSession(private val players: Array<User>, private val server: BaseServer) : GameContract.Presenter {

    private var mCurrentPlayerIndex: Int = 0
    private var mFirstChar: Char? = null

    lateinit var mExclusions: Exclusions

    lateinit var dictionary: Dictionary
    lateinit var view: GameContract.View
    val disposable = CompositeDisposable()

    private val nextPlayer: User
        get() {
            mCurrentPlayerIndex = (++mCurrentPlayerIndex) % players.size
            return players[mCurrentPlayerIndex]
        }
    private val currentPlayer: User
        get() = players[mCurrentPlayerIndex]

    override fun onAttachView(view: GameContract.View) {
        this.view = view
        for (user in players) {
            user.gameSession = this
        }
        val context = view.context()
        disposable.add(
            Exclusions.load(context)
                .doOnSuccess { mExclusions = it }
                .flatMap { Dictionary.load(context, mExclusions) }
                .doOnSuccess { dictionary = it }
                .subscribe())

        applyToFragment()
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

                mFirstChar = Utils.findLastSuitableChar(city)
                runNextPlayer()
            }
            else -> {
                Completable.fromAction { view.updateCity(city, true) }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            }
        }
    }


    private fun runNextPlayer() {
        nextPlayer.onBeginMove(mFirstChar)
    }

    override fun submit(userInput: String, callback: () -> Unit): Boolean {
        if (currentPlayer is Player) {
            (currentPlayer as Player).submit(userInput, callback)
            return true
        }
        return false
    }

    fun onLose(player: User) {
        TODO()
    }

    fun notify(msg: String) {
        view.showToast(msg)
    }

    fun dispose() {
        disposable.dispose()
        mExclusions.dispose()
        dictionary.dispose()
    }
}