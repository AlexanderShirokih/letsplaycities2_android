package ru.aleshi.letsplaycities.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    val selectedItem = MutableLiveData<Pair<Int, Int>>()
}