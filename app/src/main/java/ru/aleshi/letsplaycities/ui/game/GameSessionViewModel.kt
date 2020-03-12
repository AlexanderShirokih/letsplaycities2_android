package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.utils.Event

class GameSessionViewModel : ViewModel() {

    private val _gameSession = MutableLiveData<Event<GameSession>>()

    val gameSession: LiveData<Event<GameSession>> = _gameSession

    fun setGameSession(gameSession: GameSession) {
        _gameSession.value = Event(gameSession)
    }
}