package ru.aleshi.letsplaycities.ui.game

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Starts timer and sound effect when screen is off and fires timeoutAction when screen will be enabled.
 * If screen will be enabled before 5 seconds timeoutAction will not be executed.
 * */
class ScreenReceiver(private val timeoutAction: () -> Unit) : BroadcastReceiver() {

    private var disposable: Disposable? = null
    private var fired = false

    var sound: MediaPlayer? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> start()
            Intent.ACTION_SCREEN_ON -> stop()
        }
    }

    private fun start() {
        disposable = Completable
            .timer(500, TimeUnit.MILLISECONDS)
            .doOnComplete { sound?.start() }
            .delay(5000, TimeUnit.MILLISECONDS)
            .doOnComplete { fired = true }
            .subscribe()
    }

    private fun stop() {
        sound?.run {
            if (isPlaying)
                stop()
        }
        disposable?.dispose()

        if (fired)
            timeoutAction()
        fired = false
    }
}