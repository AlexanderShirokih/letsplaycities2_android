package ru.aleshi.letsplaycities.ui.mainmenu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_main_menu.*
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.mainmenu.MainMenuContract
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.game.GameSessionViewModel
import javax.inject.Inject

class MainMenuFragment : Fragment(R.layout.fragment_main_menu), MainMenuContract.MainMenuView {

    @Inject
    lateinit var presenter: MainMenuContract.MainMenuPresenter

    @Inject
    lateinit var prefs: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = (requireActivity() as MainActivity)
        activity.setToolbarVisibility(false)
        activity.onBackPressedDispatcher.addCallback(this@MainMenuFragment) {
            motionLayout.apply {
                if (currentState != R.id.start) {
                    val t = getTransition(R.id.endToStart)
                    setTransition(t.startConstraintSetId, t.endConstraintSetId)
                    transitionToEnd()
                } else
                    activity.finish()
            }
        }
        checkRateDialog()
        setupPlayButtonListeners()
        btn_achievements.setOnClickListener {
            Toast.makeText(
                activity,
                R.string.unavail_in_beta,
                Toast.LENGTH_SHORT
            ).show()
        }
        btn_cities_list.setOnClickListener { findNavController().navigate(R.id.showCitiesList) }
    }

    private fun checkRateDialog() {
        prefs.checkForRateDialogLaunch {
            findNavController().navigate(R.id.showRateDialog)
        }
    }

    private fun setupPlayButtonListeners() {
        val clickListener = View.OnClickListener { v ->
            val navController = findNavController()
            when (v.id) {
                R.id.btn_pva -> presenter.startGame(false)
                R.id.btn_pvp -> presenter.startGame(true)
                R.id.btn_mul -> navController.navigate(R.id.start_multiplayer_fragment)
                R.id.btn_net -> navController.navigate(
                    MainMenuFragmentDirections.startNetworkFragment(
                        BuildConfig.HOST
                    )
                )
                R.id.btn_set -> navController.navigate(R.id.start_settings_fragment)
                else -> throw IllegalStateException("Unknown button clicked!")
            }
        }
        btn_pva.setOnClickListener(clickListener)
        btn_pvp.setOnClickListener(clickListener)
        btn_mul.setOnClickListener(clickListener)
        btn_net.setOnClickListener(clickListener)
        btn_set.setOnClickListener(clickListener)
    }

    override fun startGame(gameSession: GameSession) {
        ViewModelProvider(requireParentFragment())[GameSessionViewModel::class.java].apply {
            setGameSession(gameSession)
            findNavController().navigate(R.id.start_game_fragment)
        }
    }

}