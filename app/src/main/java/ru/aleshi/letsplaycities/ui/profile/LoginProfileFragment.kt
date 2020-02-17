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
import kotlinx.android.synthetic.main.fragment_network.*
import kotlinx.android.synthetic.main.fragment_profile_login.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.ui.FetchState
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.ViewModelFactory
import javax.inject.Inject

class LoginProfileFragment : Fragment(R.layout.fragment_profile_login) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    lateinit var authorizationViewModel: AuthorizationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        authorizationViewModel =
            ViewModelProvider(this, viewModelFactory)[AuthorizationViewModel::class.java].apply {
                state.observe(this@LoginProfileFragment) { state ->
                    when (state) {
                        FetchState.LoadingState -> setLoading(true)
                        is FetchState.DataState<*> -> {
                            // Update info by string resources
                            authorizationLabel.setText(state.data as Int)
                        }
                        is FetchState.ErrorState -> {
                            setLoading(false)
                            NetworkUtils.showErrorSnackbar(state.error, this@LoginProfileFragment)
                        }
                        FetchState.FinishState -> {
                            // Successfully authorized, pop back
                            setLoading(false)
                            findNavController().popBackStack()
                        }
                    }
                }
                registerCallback()
            }
    }

    private fun setLoading(loading: Boolean) {
        groupLoading.isVisible = loading
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
