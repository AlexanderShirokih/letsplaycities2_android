package ru.aleshi.letsplaycities.base

import android.content.Context
import android.widget.Toast
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User

class GameSession(val context: Context) {

    private val players: Array<Player> = emptyArray()
    private var currentPlayerIndex: Int = 0

    var currentPlayer: User
        get() = players[currentPlayerIndex]
        set(value) {
            TODO()
        }

    val exclusions: Exclusions
    val dictionary: Dictionary

    init {
        exclusions = Exclusions()
        dictionary = Dictionary()
    }

    fun onCitySended(city: String, player: User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun submit(userInput: String, callback: () -> Unit): Boolean {
        if (currentPlayer is Player) {
            (currentPlayer as Player).submit(userInput, callback)
            return true
        }
        return false
    }

    fun notify(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}