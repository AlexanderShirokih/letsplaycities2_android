package ru.aleshi.letsplaycities.social

/**
 * Callback listener used to retrieve [SocialAccountData] from any social network
 * calls [onLoggedIn] when sign in was successful or [onError] when error happens
 */
interface SocialNetworkLoginListener {

    fun onLoggedIn(data: SocialAccountData)

    fun onError(message: String? = null)

}