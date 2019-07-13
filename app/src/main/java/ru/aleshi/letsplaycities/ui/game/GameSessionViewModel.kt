package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.base.player.User

class GameSessionViewModel : ViewModel() {

    val players : MutableLiveData<Array<User>> = MutableLiveData()

}