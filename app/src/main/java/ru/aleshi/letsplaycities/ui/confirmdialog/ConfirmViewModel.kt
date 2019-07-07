package ru.aleshi.letsplaycities.ui.confirmdialog

import androidx.lifecycle.*

class ConfirmViewModel : ViewModel() {

    val callback: MutableLiveData<Boolean> = MutableLiveData()

}