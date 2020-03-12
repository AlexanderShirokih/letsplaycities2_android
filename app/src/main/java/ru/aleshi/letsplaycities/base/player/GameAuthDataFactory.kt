package ru.aleshi.letsplaycities.base.player

import ru.aleshi.letsplaycities.base.GamePreferences
import ru.quandastudio.lpsclient.core.CredentialsProvider
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.AuthType
import ru.quandastudio.lpsclient.model.Credentials
import javax.inject.Inject

class GameAuthDataFactory @Inject constructor(private val prefs: GamePreferences) : CredentialsProvider(),
    AuthData.SaveProvider {

    override fun save(authData: AuthData) {
        prefs.edit {
            putString("sn_login", authData.login)
            putString("sn_name", authData.snType.type())
            putInt("user_id", authData.credentials.userId)
            putString("acc_hash", authData.credentials.hash)
        }
    }

    override fun load(): AuthData {
        val login = prefs.getString("sn_login", "")!!
        val userID = prefs.getInt("user_id", 0)
        val snName = prefs.getString("sn_name", "nv")!!
        val accessHash = prefs.getString("acc_hash", "")!!

        return AuthData(login, AuthType.from(snName), Credentials(userID, accessHash))
    }

    override fun loadCredentials(): Credentials = load().credentials
}