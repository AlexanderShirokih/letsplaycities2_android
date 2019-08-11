package ru.aleshi.letsplaycities.base.player

import ru.aleshi.letsplaycities.base.GamePreferences
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.PlayerData
import javax.inject.Inject

class GamePlayerDataFactory @Inject constructor(private val gameAuthDataFactory: GameAuthDataFactory) :
    PlayerData.Factory() {

    fun load(prefs: GamePreferences): PlayerData? {
        if (!prefs.isLoggedFromAnySN()) return null

        return create(gameAuthDataFactory.loadFromPreferences(prefs).apply { login = "#$userID" })
    }

    fun create(authData: AuthData): PlayerData {
        return PlayerData(authData)
    }

}