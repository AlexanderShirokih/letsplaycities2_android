package ru.aleshi.letsplaycities.base.player

import android.content.Context
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo
import java.util.concurrent.TimeUnit

class Android(context: Context, name: String, versionInfo: VersionInfo) : User(
    PictureSource(context, placeholder = R.drawable.ic_android_big),
    PlayerData.SimpleFactory().create(name, versionInfo), canUseQuickTime = false
) {

    private var mEstimatedMoves: Int = 1

    override fun onBeginMove(firstChar: Char?) {
        gameSession.disposable.add(Maybe.just(firstChar)
            .subscribeOn(Schedulers.computation())
            .delay(1500, TimeUnit.MILLISECONDS)
            .filter { mEstimatedMoves-- > 0 }
            .flatMap { gameSession.dictionary().getRandomWord(it, false) }
            .observeOn(Schedulers.io())
            .flatMapCompletable(::sendCity)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { gameSession.onSurrender() }
            .subscribe()
        )
    }

    override fun reset() {
        super.reset()
        val diff = gameSession.difficulty - 1
        mEstimatedMoves = (15 + diff.toFloat() / 3f * 70).toInt()
        mEstimatedMoves = (mEstimatedMoves..(mEstimatedMoves * 1.35f).toInt()).random()
    }

}