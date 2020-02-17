package ru.aleshi.letsplaycities.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_profile_login_no_sn.*
import kotlinx.android.synthetic.main.fragment_profile_login_no_sn.view.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.databinding.FragmentProfileLoginNoSnBinding
import ru.aleshi.letsplaycities.social.NativeAccess
import java.io.File
import javax.inject.Inject

class LoginNoSnProfileFragment : Fragment() {

    @Inject
    lateinit var mGamePreferences: GamePreferences

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGamePreferences = (requireContext().applicationContext as LPSApplication).gamePreferences
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
    }

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
                mGamePreferences.updateLastNativeLogin(input)
                findNavController().popBackStack(R.id.fragmentLoginProfile, true)
                NativeAccess.login(input, requireActivity())
            }
        }
        btnCancel.setOnClickListener { findNavController().navigateUp() }
        avatar.setOnClickListener { findNavController().navigate(R.id.showChangeAvatarDialog) }
    }

    /*
     * TODO: Does ChangeAvatarDialog do this work?
     */
    private fun processAvatar() {
//        profileViewModel.avatar.get()?.run {
//            val filesDir = requireContext().filesDir
//            val bitmap = Observable.just((this as BitmapDrawable).bitmap).share()
//
//            bitmap
//                .switchMap { Utils.saveAvatar(filesDir, it) }
//                .filter { it.isNotEmpty() }
//                .subscribe(mGamePreferences::setAvatarPath)
//
//            bitmap
//                .switchMap { Utils.saveAvatar(filesDir, it, "nv") }
//                .filter { it.isNotEmpty() }
//                .subscribe(mGamePreferences::updateLastAvatarUri)
//        }
    }

    private fun populateFields(root: View, prefs: GamePreferences) {
        root.messageInputField.setText(prefs.getLastNativeLogin())
        prefs.getLastAvatarUri()?.run {
            profileViewModel.avatarUri.set(File(this).toUri())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: What is this?
        if (!mGamePreferences.isLoggedIn())
            profileViewModel.loadDefaultAvatar()
    }
}
