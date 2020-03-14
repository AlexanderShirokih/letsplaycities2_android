package ru.aleshi.letsplaycities.ui.network.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.utils.Event
import ru.quandastudio.lpsclient.model.FriendInfo

class FriendsViewModel : ViewModel() {
    val friendsInfo: MutableLiveData<Event<FriendInfo>> = MutableLiveData()
}