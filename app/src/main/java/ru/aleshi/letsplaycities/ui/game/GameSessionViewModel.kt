package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject
import ru.aleshi.letsplaycities.base.game.GameContract

class GameSessionViewModel : ViewModel() {

    var gameSession: GameContract.Presenter? = null

    val correctedWord: MutableLiveData<Pair<String?, String?>> = MutableLiveData()

    val restart: PublishSubject<Unit> = PublishSubject.create()
}