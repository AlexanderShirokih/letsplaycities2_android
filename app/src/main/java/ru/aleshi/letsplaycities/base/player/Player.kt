package ru.aleshi.letsplaycities.base.player

import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.Dictionary
import ru.aleshi.letsplaycities.utils.Utils

class Player(authData: AuthData) : User(authData) {

    constructor(name: String) : this(AuthData.create(name))

    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    private var mFirstChar: Char? = null

    override fun onBeginMove(firstChar: Char?) {
        mFirstChar = firstChar
    }

    fun submit(userInput: String, onSuccess: () -> Unit) {
        mCompositeDisposable.add(Maybe.just(userInput)
            .map { Utils.formatCity(it) }
            .filter { it.isNotEmpty() }
            .filter { mFirstChar == null || it[0] == mFirstChar }
            .map { gameSession.mExclusions.check(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { it.second?.run { gameSession.notify(this) } }
            .filter { it.second == null }
            .map { it.first }
            .observeOn(Schedulers.computation())
            .map { gameSession.dictionary.applyCity(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ processCityResult(it, onSuccess) }, ::error)
        )
    }

    private fun processCityResult(
        data: Pair<String, Dictionary.CityResult>,
        onSuccess: () -> Unit
    ) {
        when (data.second) {
            Dictionary.CityResult.ALREADY_USED -> gameSession.notify(gameSession.view.context().getString(R.string.already_used, data.first))
            Dictionary.CityResult.CITY_NOT_FOUND -> gameSession.notify(
                gameSession.view.context().getString(
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
        return Maybe.fromCallable { Utils.loadDrawable(gameSession.view.context(), R.drawable.ic_player_big) }
    }
}