package ru.aleshi.letsplaycities.ui.confirmdialog

import androidx.lifecycle.*

class ConfirmViewModel : ViewModel() {
    class Request(val resultCode: Int, val result: Boolean)

    val callback: MutableLiveData<Request> = MutableLiveData()

}