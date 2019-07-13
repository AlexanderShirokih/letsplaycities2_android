package ru.aleshi.letsplaycities.network.lpsv3

class AuthorizationException(val banReason: String?, val connectionError: String?) : Exception()