package ru.aleshi.letsplaycities.ui.network

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.network.NetworkContract
import ru.aleshi.letsplaycities.network.NetworkPresenterImpl
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo

class NetworkViewModel : ViewModel() {

    val avatarPath: MutableLiveData<String> = MutableLiveData()

    val avatarBitmap: MutableLiveData<Bitmap> = MutableLiveData()

    val nativeLogin: MutableLiveData<String> = MutableLiveData()

    val friendsInfo: MutableLiveData<FriendsInfo> = MutableLiveData()

    val networkPresenter: NetworkContract.Presenter = NetworkPresenterImpl()
}