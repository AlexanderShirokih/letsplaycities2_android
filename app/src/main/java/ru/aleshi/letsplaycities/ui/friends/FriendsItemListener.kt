package ru.aleshi.letsplaycities.ui.friends

import ru.quandastudio.lpsclient.model.FriendInfo

interface FriendsItemListener {

    fun onFriendsItemClicked(friendsInfo: FriendInfo)

    fun onRemoveFriendsItem(friendsInfo: FriendInfo)
}