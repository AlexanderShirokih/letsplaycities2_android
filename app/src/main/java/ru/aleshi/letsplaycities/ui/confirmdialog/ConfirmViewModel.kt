package ru.aleshi.letsplaycities.ui.confirmdialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConfirmViewModel : ViewModel() {
    class Request(var resultCode: Int, val result: Boolean) {

        fun checkResultCode(requestedCode: Int): Boolean {
            return if (resultCode == requestedCode) {
                resultCode = -1
                true
            } else false
        }
    }

    val callback: MutableLiveData<Request> = MutableLiveData()

}