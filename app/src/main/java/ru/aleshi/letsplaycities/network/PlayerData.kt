package ru.aleshi.letsplaycities.network

import ru.aleshi.letsplaycities.social.AuthData


class PlayerData {
    var clientVersion: String = "unk"
    var clientBuild: Int = 80
    var canReceiveMessages: Boolean = false
    var allowSendUID: Boolean = false
    var isFriend: Boolean = false
    var userName: String? = null
    var avatar: ByteArray? = null
    var authData: AuthData? = null

    companion object Builder {
        fun create(name: String): PlayerData {
            return PlayerData().apply { userName = name }
        }
    }

    fun setBuildInfo(clientVersion: String?, clientBuild: Int) {
        if (clientVersion != null)
            this.clientVersion = clientVersion
        if (clientBuild > 0)
            this.clientBuild = clientBuild
    }

    override fun toString(): String {
        return "PlayerData{" +
                "clientVersion='" + clientVersion + '\''.toString() +
                ", clientBuild=" + clientBuild +
                ", canReceiveMessages=" + canReceiveMessages +
                ", allowSendUID=" + allowSendUID +
                ", name='" + userName + '\''.toString() +
                ", avatar=" + (if (avatar == null) "no" else "yes") +
                ", authData=" + (if (authData == null) "null" else authData.toString()) +
                '}'.toString()
    }
}