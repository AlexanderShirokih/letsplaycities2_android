package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.base.game.GameSession

class GameSessionViewModel : ViewModel() {

    val gameSession: MutableLiveData<GameSession> = MutableLiveData()

    val correctedWord: MutableLiveData<Pair<String?, String?>> = MutableLiveData()

}