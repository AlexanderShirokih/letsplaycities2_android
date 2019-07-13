package ru.aleshi.letsplaycities.social

import ru.aleshi.letsplaycities.base.AuthData

interface SocialNetworkLoginListener {

    fun onLoggedIn(data: AuthData)

    fun onError()

}