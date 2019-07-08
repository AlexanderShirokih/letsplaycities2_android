package ru.aleshi.letsplaycities.network

import ru.aleshi.letsplaycities.base.GamePreferences
import java.io.Serializable


class AuthData(
    //Public data
    var snType: AuthType,
    var snUID: String?,
    var userID: Int,
    //Private data
    var accessHash: String?,
    var accessToken: String?
) : Serializable {


    companion object Builder {
        fun load(prefs: GamePreferences, accessToken: String?): AuthData {
            return AuthData(
                AuthType.valueOf(prefs.getString("sn_name", "nv")!!),
                prefs.getString("sn_uid", null),
                prefs.getInt("user_id", 0),
                prefs.getString("acc_hash", null),
                accessToken ?: prefs.getString("acc_tkn", null)
            )
        }
    }

    fun save(prefs: GamePreferences) {
        prefs.edit()
            .putString("sn_name", snType.type())
            .putString("sn_uid", snUID)
            .putInt("user_id", userID)
            .putString("acc_hash", accessHash)
            .putString("acc_tkn", accessToken)
            .apply()
    }

    override fun toString(): String {
        return "AuthData{snType=$snType, snUID=$snUID, userID=$userID, accessHash=$accessHash, accessToken=$accessToken}"
    }
}