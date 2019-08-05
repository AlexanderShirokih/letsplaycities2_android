package ru.aleshi.letsplaycities.ui.network

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.network.NetworkContract
import ru.quandastudio.lpsclient.model.FriendsInfo
import javax.inject.Inject

class NetworkViewModel @Inject constructor(val networkPresenter: NetworkContract.Presenter) : ViewModel() {

    val avatarPath: MutableLiveData<String> = MutableLiveData()

    val avatarBitmap: MutableLiveData<Bitmap> = MutableLiveData()

    val nativeLogin: MutableLiveData<String> = MutableLiveData()

    val friendsInfo: MutableLiveData<FriendsInfo> = MutableLiveData()
}