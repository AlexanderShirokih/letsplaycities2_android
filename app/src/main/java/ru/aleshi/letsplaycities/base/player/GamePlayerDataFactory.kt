package ru.aleshi.letsplaycities.base.player

import ru.aleshi.letsplaycities.base.GamePreferences
import ru.quandastudio.lpsclient.model.PlayerData
import javax.inject.Inject

class GamePlayerDataFactory @Inject constructor(private val gameAuthDataFactory: GameAuthDataFactory) :
    PlayerData.SimpleFactory() {

    fun load(prefs: GamePreferences): PlayerData? {
        if (!prefs.isLoggedFromAnySN()) return null

        return PlayerData().apply {
            authData = gameAuthDataFactory.loadFromPreferences(prefs)
            userName = "#" + authData!!.userID
        }
    }

}