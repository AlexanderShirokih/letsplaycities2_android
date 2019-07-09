package ru.aleshi.letsplaycities.ui.network

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AvatarViewModel : ViewModel() {

    class Test : MutableLiveData<String>() {
        override fun setValue(value: String?) {
            Log.d("TAG", "Set value=$value")
            if (value == null)
                Exception().printStackTrace()
            super.setValue(value)
        }

        override fun postValue(value: String?) {
            Log.d("TAG", "Post value=$value")
            super.postValue(value)
        }
    }

    val avatarPath: MutableLiveData<String> = Test()

    val avatarBitmap: MutableLiveData<Bitmap> = MutableLiveData()

    val nativeLogin: MutableLiveData<String> = MutableLiveData()
}