package ru.aleshi.letsplaycities.base.game

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.aleshi.letsplaycities.base.GamePreferences

class LocalServer(gamePreferences: GamePreferences) : BaseServer() {

    private var result: PublishSubject<Pair<WordResult, String>> = PublishSubject.create()
    private val timeLimit = gamePreferences.getTimeLimit()

    override fun broadcastResult(city: String) {
        // We trust our local users
        result.onNext(WordResult.ACCEPTED to city)
    }

    override fun getWordsResult(): Observable<Pair<WordResult, String>> = result

    override fun getTimeLimit(): Long = timeLimit

}