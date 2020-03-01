package ru.aleshi.letsplaycities.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_profile_login.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.aleshi.letsplaycities.ui.FetchState
import ru.aleshi.letsplaycities.ui.MainActivity
import javax.inject.Inject

class LoginProfileFragment : Fragment(R.layout.fragment_profile_login) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        ViewModelProvider(this, viewModelFactory)[AuthorizationViewModel::class.java].apply {
            registerCallback()
            loading.observe(this@LoginProfileFragment, ::setLoading)
            state.observe(this@LoginProfileFragment) { state ->
                when (state) {
                    is FetchState.DataState<*> -> {
                        // Update info by string resources
                        authorizationLabel.setText(state.data as Int)
                    }
                    is FetchState.ErrorState ->
                        NetworkUtils.showErrorSnackbar(state.error, this@LoginProfileFragment)
                    FetchState.FinishState -> {
                        // Successfully authorized, pop back
                        findNavController().popBackStack()
                    }
                }
            }
        }
        ViewModelProvider(requireActivity())[ProfileViewModel::class.java].nativeEvents.observe(this) {
            it.getContentIfNotHandled()?.run {
                SocialNetworkManager.login(ServiceType.NV, requireActivity())
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        loadingGroup.isVisible = loading
        socialButtonsGroup.isVisible = !loading
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
