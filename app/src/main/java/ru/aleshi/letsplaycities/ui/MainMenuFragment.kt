package ru.aleshi.letsplaycities.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.makeInAnimation
import android.view.animation.AnimationUtils.makeOutAnimation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_main_menu.*
import ru.aleshi.letsplaycities.BadTokenException
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GameMode
import ru.aleshi.letsplaycities.utils.IntegrityChecker
import ru.aleshi.letsplaycities.utils.Utils


class MainMenuFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        (activity as MainActivity).setToolbarVisibility(false)
        Utils.checkRateDialog(activity)
        makeInAnimation()
        createViewListener().run {
            btn_pva.setOnClickListener(this)
            btn_pvp.setOnClickListener(this)
            btn_mul.setOnClickListener(this)
            btn_net.setOnClickListener(this)
            btn_set.setOnClickListener(this)
        }
    }

    override fun onResume() {
        super.onResume()
        makeInAnimation()
    }

    private fun createViewListener(): View.OnClickListener {
        return View.OnClickListener { v ->
            makeOutAnimation(Runnable {
                if (IntegrityChecker().test()) {
                    try {
                        throw BadTokenException()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        System.exit(0)
                    }
                }

                val navController = findNavController()
                when (v.id) {
                    R.id.btn_mul -> navController.navigate(R.id.start_multiplayer_fragment)
                    R.id.btn_net -> navController.navigate(R.id.start_network_fragment)
                    R.id.btn_pva -> navController.navigate(MainMenuFragmentDirections.startGameFragment(GameMode.MODE_PVA))
                    R.id.btn_pvp -> navController.navigate(MainMenuFragmentDirections.startGameFragment(GameMode.MODE_PVP))
                    R.id.btn_set -> navController.navigate(R.id.start_settings_fragment)
                    else -> throw IllegalStateException("Unknown button clicked!")
                }
            })
        }
    }

    private fun makeInAnimation() {
        startAnimation(btn_pva, 0, true, null)
        startAnimation(btn_pvp, 200, true, null)
        startAnimation(btn_net, 400, true, null)
        startAnimation(btn_mul, 600, true, null)
        startAnimation(btn_set, 800, true, null)
    }

    private fun makeOutAnimation(fb: Runnable) {
        startAnimation(btn_set, 0, false, null)
        startAnimation(btn_mul, 200, false, null)
        startAnimation(btn_net, 400, false, null)
        startAnimation(btn_pvp, 600, false, null)
        startAnimation(btn_pva, 800, false, fb)
    }

    private fun startAnimation(v: View, delay: Int, inAnimation: Boolean, fb: Runnable?) {
        val animation =
            if (inAnimation) makeInAnimation(requireContext(), true) else makeOutAnimation(requireContext(), true)
        animation.duration = 500
        animation.startOffset = delay.toLong()
        if (!inAnimation) {
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    v.visibility = View.INVISIBLE
                    fb?.run()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        } else
            v.visibility = View.VISIBLE
        v.startAnimation(animation)
    }

}