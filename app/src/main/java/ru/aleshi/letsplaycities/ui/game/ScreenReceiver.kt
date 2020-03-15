package ru.aleshi.letsplaycities.ui.game

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import kotlinx.coroutines.*

/**
 * Starts timer and sound effect when screen is off and fires timeoutAction when screen will be enabled.
 * If screen will be enabled before 5 seconds timeoutAction will not be executed.
 * */
class ScreenReceiver(private val timeoutAction: () -> Unit) : BroadcastReceiver() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var fired = false

    var sound: MediaPlayer? = null
    var active: Boolean = false

    override fun onReceive(context: Context, intent: Intent) {
        if (active)
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> start()
                Intent.ACTION_SCREEN_ON -> stop()
            }
    }

    private fun start() {
        scope.launch {
            delay(500)
            sound?.start()
            delay(5000)
            fired = true
        }
    }

    private fun stop() {
        scope.coroutineContext.cancelChildren()

        sound?.run {
            if (isPlaying)
                stop()
        }

        if (fired)
            timeoutAction()
        fired = false
    }

    /**
     * Use this function to release sound resource when screen receiver no longer needed.
     */
    fun dispose() {
        sound?.release()
        sound = null
    }
}