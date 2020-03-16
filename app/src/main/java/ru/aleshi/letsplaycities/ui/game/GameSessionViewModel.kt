package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crashlytics.android.Crashlytics
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.utils.Event

/**
 * ViewModel class for sharing [gameSession] instance between fragments.
 * Destination is GameFragment
 */
class GameSessionViewModel : ViewModel() {

    private val _gameSession = MutableLiveData<Event<GameSession>>()

    val gameSession: LiveData<Event<GameSession>> = _gameSession

    /**
     * Updates current session [LiveData]
     */
    fun setGameSession(gameSession: GameSession) {
        logGameSessionInfoToCrashlythics(gameSession)
        _gameSession.value = Event(gameSession)
    }

    /**
     * Logs current session params to Crashlythics
     */
    private fun logGameSessionInfoToCrashlythics(gameSession: GameSession) {
        Crashlytics.setString("current_mode", gameSession.gameMode.name)
        gameSession.users.firstOrNull { it is Player }?.apply {
            Crashlytics.setUserIdentifier(this.credentials.userId.toString())
            Crashlytics.setUserName(this.name)
            Crashlytics.setString("pl_pic_hash", this.playerData.pictureHash)
            Crashlytics.setString("pl_sn_name", this.playerData.authData.snType.name)
            Crashlytics.setBool("pl_is_friend_to_opp", this.playerData.isFriend)
        }
    }
}