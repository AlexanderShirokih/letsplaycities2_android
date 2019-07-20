package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameResultViewModel : ViewModel() {

    enum class SelectedItem { SHARE, REPLAY, MENU }

    class SelectionResult(private val result: SelectedItem) {

        private var used: Boolean = false

        fun result(): SelectedItem? {
            return if (used) null else {
                used = true
                result
            }
        }
    }


    val result: MutableLiveData<SelectionResult> = MutableLiveData()
}