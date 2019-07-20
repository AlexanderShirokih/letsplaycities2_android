package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.base.game.GameContract

class GameSessionViewModel : ViewModel() {

    val gameSession: MutableLiveData<GameContract.Presenter> = MutableLiveData()

    val correctedWord: MutableLiveData<Pair<String?, String?>> = MutableLiveData()

}