package ru.aleshi.letsplaycities.base.player

import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.Utils
import java.util.concurrent.TimeUnit

class Android(name: String) : User(AuthData.create(name)) {

    override fun onBeginMove(firstChar: Char?) {
        gameSession.disposable.add(Maybe.just(firstChar)
            .subscribeOn(Schedulers.computation())
            .delay(300, TimeUnit.MILLISECONDS)
            .flatMap { gameSession.dictionary().getRandomWord(it, false) }
            .subscribe(::sendCity, {}, { gameSession.onLose(this) })
        )
    }

    override fun getAvatar(): Maybe<Drawable> {
        return Maybe.fromCallable { Utils.loadDrawable(gameSession.view.context(), R.drawable.ic_android_big) }
    }

}