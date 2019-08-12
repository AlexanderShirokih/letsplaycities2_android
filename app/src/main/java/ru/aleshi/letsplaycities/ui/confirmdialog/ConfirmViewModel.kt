package ru.aleshi.letsplaycities.ui.confirmdialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class ConfirmViewModel @Inject constructor(): ViewModel() {


    class Request(var resultCode: Int, val result: Boolean) {

        fun checkWithResultCode(requestedCode: Int): Boolean {
            return if (resultCode == requestedCode && result) {
                resultCode = -1
                true
            } else false
        }
        fun checkAnyWithResultCode(requestedCode: Int): Boolean {
            return if (resultCode == requestedCode) {
                resultCode = -1
                true
            } else false
        }
    }

    val callback: MutableLiveData<Request> = MutableLiveData()

}