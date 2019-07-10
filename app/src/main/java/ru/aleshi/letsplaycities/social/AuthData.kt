package ru.aleshi.letsplaycities.social

import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.network.AuthType
import java.io.Serializable


class AuthData(var login: String, var snUID: String, var snName: String, var accessToken: String) : Serializable {
    var accessHash: String? = null
    var userID: Int = 0

    private val hash: String = SocialUtils.md5("$login,$snUID,$snName,")

    fun saveToPreferences(prefs: GamePreferences) {
        val editor = prefs.edit()
        editor.putString("hash", hash)
        editor.putString("sn_login", login)
        editor.putString("sn_uid", snUID)
        editor.putString("sn_name", snName)
        editor.putString("acc_tkn", accessToken)
        editor.putInt("user_id", userID)
        editor.putString("acc_hash", accessHash)
        editor.apply()
    }

    override fun toString(): String {
        return "AuthData(login='$login', snUID='$snUID', snName='$snName', accessToken=$accessToken, accessHash=$accessHash, userID=$userID)"
    }

    fun getSnType() = AuthType.valueOf(snName)

    companion object {
        fun loadFromPreferences(prefs: GamePreferences): AuthData {
            val login = prefs.getString("sn_login", "")!!
            val id = prefs.getString("sn_uid", "")!!
            val snName = prefs.getString("sn_name", "nv")!!
            val accessToken = prefs.getString("acc_tkn", null)!!

            return AuthData(login, id, snName, accessToken).apply {
                accessHash = prefs.getString("acc_hash", null)
                userID = prefs.getInt("user_id", 0)
            }
        }
    }
}
