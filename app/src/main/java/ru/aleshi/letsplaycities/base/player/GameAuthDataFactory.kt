package ru.aleshi.letsplaycities.base.player

import ru.aleshi.letsplaycities.base.GamePreferences
import ru.quandastudio.lpsclient.model.AuthData
import javax.inject.Inject

class GameAuthDataFactory @Inject constructor() : AuthData.SimpleFactory() {

    class GameSaveProvider(private val prefs: GamePreferences) : AuthData.SaveProvider {

        override fun save(authData: AuthData) {
            val editor = prefs.edit()
            editor.putString("hash", authData.hash)
            editor.putString("sn_login", authData.login)
            editor.putString("sn_uid", authData.snUID)
            editor.putString("sn_name", authData.snName)
            editor.putString("acc_tkn", authData.accessToken)
            editor.putInt("user_id", authData.userID)
            editor.putString("acc_hash", authData.accessHash)
            editor.apply()
        }

    }

    fun loadFromPreferences(prefs: GamePreferences): AuthData {
        val login = prefs.getString("sn_login", "")!!
        val id = prefs.getString("sn_uid", "")!!
        val snName = prefs.getString("sn_name", "nv")!!
        val accessToken = prefs.getString("acc_tkn", "")!!

        return AuthData(login, id, snName, accessToken).apply {
            accessHash = prefs.getString("acc_hash", null)
            userID = prefs.getInt("user_id", 0)
        }
    }
}