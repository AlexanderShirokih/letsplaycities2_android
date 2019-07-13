package ru.aleshi.letsplaycities.base.player

import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.AuthData
import ru.aleshi.letsplaycities.base.Dictionary
import ru.aleshi.letsplaycities.utils.Utils

class Player(authData: AuthData) : User(authData) {

    constructor(name: String) : this(AuthData.create(name))

    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    private var mPreviousFirstChar: Char? = null

    override fun onBeginMove(firstChar: Char?) {
        mPreviousFirstChar = firstChar
    }

    fun submit(userInput: String, onSuccess: () -> Unit) {
        mCompositeDisposable.add(Maybe.just(userInput)
            .map { it.trim().toLowerCase() }
            .filter { it.isEmpty() }
            .filter { mPreviousFirstChar == null || it.last() == mPreviousFirstChar }
            .map { gameSession.mExclusions.check(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { it.second?.run { gameSession.notify(this) } }
            .map { it.first }
            .observeOn(Schedulers.computation())
            .map { gameSession.mDictionary.applyCity(userInput) }
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

    override fun getAvatar(): Maybe<Drawable> {
        return Maybe.fromCallable { Utils.loadDrawable(gameSession.context, R.drawable.ic_player_big) }
    }
}