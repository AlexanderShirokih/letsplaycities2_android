package ru.aleshi.letsplaycities.social

interface SocialNetworkLoginListener {

    fun onLoggedIn(info: SocialInfo, accessToken: String)

    fun onError()

}