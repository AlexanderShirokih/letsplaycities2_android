package ru.aleshi.letsplaycities.base.player

import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.dictionary.Dictionary
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.utils.StringUtils
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo

class Player(
    playerData: PlayerData,
    picasso: Picasso
) :
    User(
        playerData,
        PictureSource(
            picasso = picasso,
            uri = Utils.getPictureUri(
                playerData.authData.credentials.userId,
                playerData.pictureHash
            ),
            placeholder = R.drawable.ic_player_big
        ),
        hasUserInput = true
    ) {

    constructor(
        picasso: Picasso,
        name: String,
        versionInfo: VersionInfo
    ) : this(
        PlayerData.SimpleFactory().create(name, versionInfo),
        picasso
    )

    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    private var mFirstChar: Char? = null

    override fun onBeginMove(firstChar: Char?) {
        mFirstChar = firstChar
    }

    class CityNotFoundException(val city: String) : Exception()

    override fun onUserInput(userInput: String, onSuccess: () -> Unit) {
        mCompositeDisposable.add(Maybe.just(userInput)
            .map { StringUtils.formatCity(it) }
            .filter { it.isNotEmpty() }
            .filter { mFirstChar == null || it[0] == mFirstChar }
            .map { gameSession.mExclusions.check(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { it.second?.run { gameSession.notify(this) } }
            .filter { it.second == null }
            .map { it.first }
            .observeOn(Schedulers.computation())
            .flatMap {
                var city = it
                Maybe.just(0)
                    .map { city }
                    .map { c -> gameSession.dictionary().applyCity(c) }
                    .flatMap { res ->
                        if (res.second == Dictionary.CityResult.CITY_NOT_FOUND) Maybe.error(
                            CityNotFoundException(res.first)
                        ) else Maybe.just(res)
                    }
                    .retry { c, t ->
                        city = StringUtils.replaceWhitespaces(city)
                        c < 2 && t is CityNotFoundException
                    }
                    .onErrorResumeNext { t: Throwable ->
                        if (t is CityNotFoundException)
                            Maybe.just(t.city to Dictionary.CityResult.CITY_NOT_FOUND)
                        else
                            Maybe.error<Pair<String, Dictionary.CityResult>>(t)
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ processCityResult(it, onSuccess) }, ::error)
        )
    }

    private fun processCityResult(
        data: Pair<String, Dictionary.CityResult>,
        onSuccess: () -> Unit
    ) {
        when (data.second) {
            Dictionary.CityResult.ALREADY_USED -> gameSession.notify(
                gameSession.view.context().getString(
                    R.string.already_used,
                    StringUtils.toTitleCase(data.first)
                )
            )
            Dictionary.CityResult.CITY_NOT_FOUND -> gameSession.correct(
                data.first,
                gameSession.view.context().getString(
                    R.string.city_not_found,
                    StringUtils.toTitleCase(data.first)
                )
            )
            Dictionary.CityResult.OK -> {
                sendCity(data.first)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        onSuccess()
                    }.subscribe(
                        {
                        },
                        { err ->
                            gameSession.notify(
                                gameSession.view.context().getString(
                                    R.string.unk_error,
                                    err.message
                                )
                            )
                        })
            }
        }
    }

}