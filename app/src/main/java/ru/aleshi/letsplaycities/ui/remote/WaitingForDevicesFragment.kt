package ru.aleshi.letsplaycities.ui.remote

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_waiting_for_devices.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.remote.RemoteContract
import ru.aleshi.letsplaycities.remote.RemotePresenter
import ru.aleshi.letsplaycities.ui.game.GameSessionViewModel
import javax.inject.Inject

class WaitingForDevicesFragment : Fragment(R.layout.fragment_waiting_for_devices),
    RemoteContract.View {

    @Inject
    lateinit var remotePresenter: RemoteContract.Presenter

    @Inject
    lateinit var gamePreferences: GamePreferences

    private var mGameSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        remotePresenter.onApplyView(this)
        if (gamePreferences.isSoundEnabled()) {
            mGameSound = MediaPlayer.create(activity, R.raw.begin)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        remotePresenter.onStart()
    }

    override fun onStartGame(gameSession: GameSession) {
        ViewModelProvider(requireParentFragment())[GameSessionViewModel::class.java].apply {
            setGameSession(gameSession)
            this.gameSession.observe(this@WaitingForDevicesFragment) {
                if (!it.hasBeenHandled) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        val controller = findNavController()
                        while (R.id.waitingForDevicesFragment != controller.currentDestination?.id)
                            delay(100)
                        mGameSound?.start()
                        controller.navigate(R.id.start_game_fragment)
                    }
                }
            }
        }
    }

    override fun setStatus(status: RemotePresenter.Status) {
        tvStatus.setText(
            when (status) {
                RemotePresenter.Status.WAITING_FOR_OPPONENTS -> R.string.waiting_for_opp
            }
        )
    }

    override fun onError(message: String) {
        Snackbar
            .make(requireView(), getString(R.string.error_err, message), Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        remotePresenter.onStop()
    }

}
