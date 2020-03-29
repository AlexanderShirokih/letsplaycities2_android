package ru.aleshi.letsplaycities.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.databinding.FragmentProfileViewBinding
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.quandastudio.lpsclient.core.CredentialsProvider
import javax.inject.Inject

class ViewProfileFragment : Fragment() {
    private lateinit var mProfileViewModel: ProfileViewModel

    @Inject
    lateinit var gamePreferences: GamePreferences

    @Inject
    lateinit var credentialsProvider: CredentialsProvider

    private var mVisited = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        mProfileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
    }

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
            profile = mProfileViewModel
            fragment = this@ViewProfileFragment
        }.root
    }
}
