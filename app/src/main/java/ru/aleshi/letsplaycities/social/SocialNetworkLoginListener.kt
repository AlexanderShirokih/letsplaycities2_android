package ru.aleshi.letsplaycities.social

interface SocialNetworkLoginListener {

    fun onLoggedIn(data: SocialAccountData)

    fun onError()

}