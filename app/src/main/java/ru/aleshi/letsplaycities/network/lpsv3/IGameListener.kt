package ru.aleshi.letsplaycities.network.lpsv3

interface IGameListener {
    fun onMessage(isSystem: Boolean, message: String)

    fun onWord(res: WordResult, word: String)

    fun onSync(time: Int)

    fun onDisconnected(leaved: Boolean)

    fun onServerCommand(action: Byte)

    fun onTimeOut()

    fun onFriendsAction(action: String)
}