package ru.aleshi.letsplaycities.base.player

import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.Dictionary
import ru.aleshi.letsplaycities.base.GameSession
import ru.aleshi.letsplaycities.social.AuthData

class Player(gameSession: GameSession, authData: AuthData) : User(gameSession, authData) {

    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    private var mPreviousFirstChar: Char? = null

    override fun onBeginMove(firstChar: Char?) {
        mPreviousFirstChar = firstChar
    }

    fun submit(userInput: String, onSuccess: () -> Unit) {
        mCompositeDisposable.add(Maybe.just(userInput)
            .map { it.trim().toLowerCase() }
            .filter { mPreviousFirstChar == null || it.last() == mPreviousFirstChar }
            .map { gameSession.exclusions.check(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { it.second?.run { gameSession.notify(this) } }
            .map { it.first }
            .observeOn(Schedulers.computation())
            .map { gameSession.dictionary.checkCity(userInput) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ processCityResult(it, onSuccess) }, ::error)
        )
    }

    private fun processCityResult(
        data: Pair<String, Dictionary.CityResult>,
        onSuccess: () -> Unit
    ) {
        when (data.second) {
            Dictionary.CityResult.ALREADY_USED -> gameSession.notify(gameSession.context.getString(R.string.already_used))
            Dictionary.CityResult.CITY_NOT_FOUND -> gameSession.notify(
                gameSession.context.getString(
                    R.string.city_not_found,
                    data.first
                )
            )
            Dictionary.CityResult.OK -> {
                onSuccess()
                sendCity(data.first)
            }
        }
    }
}