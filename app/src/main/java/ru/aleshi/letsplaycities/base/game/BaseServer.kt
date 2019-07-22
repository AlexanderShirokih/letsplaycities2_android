package ru.aleshi.letsplaycities.base.game

import io.reactivex.Observable
import io.reactivex.Single
import ru.aleshi.letsplaycities.network.lpsv3.LPSMessage

abstract class BaseServer {

    abstract fun getWordsResult(): Observable<Pair<WordResult, String>>

    abstract fun broadcastResult(city: String)

    abstract fun getTimeLimit(): Long
}