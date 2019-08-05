package ru.aleshi.letsplaycities.social

import ru.quandastudio.lpsclient.model.AuthData

interface SocialNetworkLoginListener {

    fun onLoggedIn(data: AuthData)

    fun onError()

}