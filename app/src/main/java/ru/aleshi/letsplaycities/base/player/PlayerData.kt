package ru.aleshi.letsplaycities.base.player

import ru.aleshi.letsplaycities.base.GamePreferences

class PlayerData {
    var clientVersion: String = "unk"
    var clientBuild: Int = 80
    var userName: String? = null
    var canReceiveMessages: Boolean = false
    var allowSendUID: Boolean = false
    var isFriend: Boolean = false
    var avatar: ByteArray? = null
    var authData: AuthData? = null
    var firebaseToken: String? = null

    companion object Factory {
        fun create(login: String): PlayerData {
            return PlayerData().apply { userName = login }
        }

        fun load(prefs: GamePreferences): PlayerData? {
            if (!prefs.isLoggedFromAnySN()) return null

            return PlayerData().apply {
                authData = AuthData.loadFromPreferences(prefs)
                userName = "#" + authData!!.userID
            }
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
                ", userName='" + userName + '\''.toString() +
                ", avatar=" + (if (avatar == null) "no" else "yes") +
                ", authData=" + (if (authData == null) "null" else authData.toString()) +
                '}'.toString()
    }
}