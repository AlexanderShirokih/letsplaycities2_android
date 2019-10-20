package ru.aleshi.letsplaycities.ui.profile

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_profile_login.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkLoginListener
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.quandastudio.lpsclient.model.AuthData

class LoginProfileFragment : Fragment(R.layout.fragment_profile_login) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gamePrefs = (requireContext().applicationContext as LPSApplication).gamePreferences

        SocialNetworkManager.registerCallback(object : SocialNetworkLoginListener {
            override fun onLoggedIn(data: AuthData) {
                GameAuthDataFactory.GameSaveProvider(gamePrefs).save(data)
                Handler().postDelayed({
                    findNavController().popBackStack()
                }, 1000)
            }

            override fun onError() {
                Handler().postDelayed({
                    Snackbar.make(requireView(), R.string.auth_error, Snackbar.LENGTH_LONG).show()
                }, 100)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity() as MainActivity
        btnVk.setSocialListener(activity, ServiceType.VK)
        btnOk.setSocialListener(activity, ServiceType.OK)
        btnFb.setSocialListener(activity, ServiceType.FB)
        btnGl.setSocialListener(activity, ServiceType.GL)
        btnLoginNoSn.setOnClickListener { findNavController().navigate(R.id.actionLoginNoSn) }
    }

    private fun ImageButton.setSocialListener(activity: MainActivity, serviceType: ServiceType) =
        setOnClickListener(OnSocialButtonClickedListener(activity, serviceType))
}
