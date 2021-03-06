package ru.aleshi.letsplaycities.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_profile_login_no_sn.*
import kotlinx.android.synthetic.main.fragment_profile_login_no_sn.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.databinding.FragmentProfileLoginNoSnBinding
import ru.aleshi.letsplaycities.utils.Event
import javax.inject.Inject

class LoginNoSnProfileFragment : DaggerFragment() {

    @Inject
    lateinit var mGamePreferences: GamePreferences

    private val profileViewModel: ProfileViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentProfileLoginNoSnBinding.inflate(inflater, container, false).apply {
            profile = profileViewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateFields(view, mGamePreferences)
        btnEnter.setOnClickListener {
            val input = messageInputField.text.toString()
            if (input.length in 4..20) {
                mGamePreferences.lastNativeLogin = input
                profileViewModel.nativeEvents.postValue(Event(Unit))
                findNavController().popBackStack(R.id.fragmentLoginProfile, false)
            }
        }
        btnCancel.setOnClickListener { findNavController().navigateUp() }
        avatar.setOnClickListener { findNavController().navigate(R.id.showChangeAvatarDialog) }
    }

    private fun populateFields(root: View, prefs: GamePreferences) {
        root.messageInputField.setText(prefs.lastNativeLogin)
        profileViewModel.avatarUri.set(prefs.lastAvatarUri?.toString()?.toUri() ?: Uri.EMPTY)
    }

}
