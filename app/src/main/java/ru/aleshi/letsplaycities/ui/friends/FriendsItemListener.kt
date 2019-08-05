package ru.aleshi.letsplaycities.ui.friends

import ru.quandastudio.lpsclient.model.FriendsInfo

interface FriendsItemListener {

    fun onFriendsItemClicked(friendsInfo: FriendsInfo)

    fun onRemoveFriendsItem(friendsInfo: FriendsInfo)
}