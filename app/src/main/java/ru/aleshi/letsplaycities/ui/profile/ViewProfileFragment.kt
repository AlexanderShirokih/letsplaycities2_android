package ru.aleshi.letsplaycities.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.databinding.FragmentProfileViewBinding
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.quandastudio.lpsclient.core.CredentialsProvider
import javax.inject.Inject

class ViewProfileFragment : Fragment() {
    private lateinit var mGamePreferences: GamePreferences
    private lateinit var mProfileViewModel: ProfileViewModel

    @Inject
    lateinit var credentialsProvider: CredentialsProvider

    private var mVisited = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        mProfileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
    }

    fun onLogout(view: View) {
        SocialNetworkManager.logout(mGamePreferences, credentialsProvider)
        findNavController().navigate(R.id.actionLogin)
    }

    override fun onResume() {
        super.onResume()
        if (mVisited)
            findNavController().popBackStack()
        else {
            mVisited = true
            if (!mGamePreferences.isLoggedIn())
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
