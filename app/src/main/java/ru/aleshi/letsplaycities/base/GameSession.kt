package ru.aleshi.letsplaycities.base

import android.content.Context
import android.util.Log
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.ui.game.GameViewModel

class GameSession(val context: Context, private val players: Array<User>, private val gameViewModel: GameViewModel) {

    private val mDisposable = CompositeDisposable()
    private var currentPlayerIndex: Int = 0

    lateinit var mExclusions: Exclusions
    lateinit var mDictionary: Dictionary

    val currentPlayer: User
        get() = players[currentPlayerIndex]

    init {
        for (user in players) {
            user.gameSession = this
        }

        mDisposable.add(Exclusions.load(context)
            .doOnSuccess { mExclusions = it }
            .flatMap { Dictionary.load(context, mExclusions) }
            .doOnSuccess { mDictionary = it }
            .subscribe())

        applyToFragment()
    }

    private fun applyToFragment() {
        // Apply to the left
        updateLabel(players[0], true)
        mDisposable.add(players[0].getAvatar()
            .subscribe { gameViewModel.avatarLeft.set(it) })

        // Apply to the right
        updateLabel(players[1], false)
        mDisposable.add(players[1].getAvatar()
            .subscribe { gameViewModel.avatarRight.set(it) })
    }

    private fun updateLabel(user: User, isLeft: Boolean) {
        val field =
            if (isLeft)
                gameViewModel.infoLeft
            else
                gameViewModel.infoRight
        field.set(user.info)
    }


    fun onCitySended(city: String, player: User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun submit(userInput: String, callback: () -> Unit): Boolean {
        if (currentPlayer is Player) {
            Log.d("TAG", "SUBMITED=$userInput")
            (currentPlayer as Player).submit(userInput, callback)
            return true
        }
        return false
    }

    fun notify(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun dispose() {
        mDisposable.dispose()
        mExclusions.dispose()
        mDictionary.dispose()
    }
}