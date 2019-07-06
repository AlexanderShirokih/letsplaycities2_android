package ru.aleshi.letsplaycities2.ui.confirmdialog

import androidx.lifecycle.*

class ConfirmViewModel : ViewModel() {

    val callback: MutableLiveData<Boolean> = MutableLiveData()

}