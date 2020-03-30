package ru.aleshi.letsplaycities.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.databinding.FragmentProfileViewBinding
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.quandastudio.lpsclient.core.CredentialsProvider
import javax.inject.Inject

class ViewProfileFragment : DaggerFragment() {

    private val profileViewModel: ProfileViewModel by viewModels({ requireActivity() })

    @Inject
    lateinit var gamePreferences: GamePreferences

    @Inject
    lateinit var credentialsProvider: CredentialsProvider

    private var mVisited = false

    fun onLogout(view: View) {
        lifecycleScope.launch {
            view.isClickable = false
            SocialNetworkManager.logout(requireActivity(), gamePreferences, credentialsProvider)
            view.isClickable = true
            findNavController().navigate(R.id.actionLogin)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mVisited)
            findNavController().popBackStack()
        else {
            mVisited = true
            if (!gamePreferences.isLoggedIn())
                findNavController().navigate(R.id.actionLogin)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentProfileViewBinding.inflate(inflater, container, false).apply {
            profile = profileViewModel
            fragment = this@ViewProfileFragment
        }.root
    }
}
