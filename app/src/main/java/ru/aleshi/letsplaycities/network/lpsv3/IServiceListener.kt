package ru.aleshi.letsplaycities.network.lpsv3

interface IServiceListener {

    fun onFriendsList(list: ArrayList<FriendsInfo>)
}
