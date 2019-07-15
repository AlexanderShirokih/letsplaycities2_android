package ru.aleshi.letsplaycities.base.game

import io.reactivex.Single
import ru.aleshi.letsplaycities.base.game.WordResult

abstract class BaseServer {

    abstract fun broadcastResult(city: String): Single<WordResult>

}