package ru.aleshi.letsplaycities.social

import ru.aleshi.letsplaycities.base.player.AuthData

interface SocialNetworkLoginListener {

    fun onLoggedIn(data: AuthData)

    fun onError()

}