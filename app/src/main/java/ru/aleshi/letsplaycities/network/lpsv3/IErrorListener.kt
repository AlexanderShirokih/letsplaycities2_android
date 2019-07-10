package ru.aleshi.letsplaycities.network.lpsv3

interface IErrorListener {
    fun onException(ex: Exception)

    fun onInvalidMessage()
}
