package ru.aleshi.letsplaycities.ui.friends

import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo

interface FriendsItemListener {

    fun onFriendsItemClicked(friendsInfo: FriendsInfo)

    fun onRemoveFriendsItem(friendsInfo: FriendsInfo)
}