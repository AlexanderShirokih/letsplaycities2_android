package ru.aleshi.letsplaycities.ui.network

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo
import ru.aleshi.letsplaycities.network.lpsv3.NetworkRepository

class NetworkViewModel : ViewModel() {

    val avatarPath: MutableLiveData<String> = MutableLiveData()

    val avatarBitmap: MutableLiveData<Bitmap> = MutableLiveData()

    val nativeLogin: MutableLiveData<String> = MutableLiveData()

    val friendsInfo: MutableLiveData<FriendsInfo> = MutableLiveData()

    val networkRepository: MutableLiveData<NetworkRepository> = MutableLiveData()
}