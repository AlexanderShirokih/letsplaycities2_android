package ru.aleshi.letsplaycities.ui.network.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.quandastudio.lpsclient.model.FriendInfo

class FriendsViewModel : ViewModel() {
    val friendsInfo: MutableLiveData<FriendInfo> = MutableLiveData()
}