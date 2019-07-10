package ru.aleshi.letsplaycities.social

interface SocialNetworkLoginListener {

    fun onLoggedIn(data: AuthData)

    fun onError()

}