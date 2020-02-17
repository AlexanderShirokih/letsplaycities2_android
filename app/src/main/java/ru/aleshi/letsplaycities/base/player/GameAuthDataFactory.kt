package ru.aleshi.letsplaycities.base.player

import ru.aleshi.letsplaycities.LPSApplication
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.AuthType
import ru.quandastudio.lpsclient.model.Credentials
import javax.inject.Inject

class GameAuthDataFactory @Inject constructor(application: LPSApplication) : AuthData.SaveProvider {

    private val prefs = application.gamePreferences

    override fun save(authData: AuthData) {
        val editor = prefs.edit()
        editor.putString("sn_login", authData.login)
        editor.putString("sn_name", authData.snType.type())
        editor.putInt("user_id", authData.credentials.userId)
        editor.putString("acc_hash", authData.credentials.hash)
        editor.apply()
    }

    override fun load(): AuthData {
        val login = prefs.getString("sn_login", "")
        val userID = prefs.getInt("user_id", 0)
        val snName = prefs.getString("sn_name", "nv")
        val accessHash = prefs.getString("acc_hash", "")

        return AuthData(login, AuthType.from(snName), Credentials(userID, accessHash))
    }
}