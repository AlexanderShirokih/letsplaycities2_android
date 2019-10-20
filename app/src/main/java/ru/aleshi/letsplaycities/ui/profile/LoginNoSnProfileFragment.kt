package ru.aleshi.letsplaycities.ui.profile

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_profile_login_no_sn.*
import kotlinx.android.synthetic.main.fragment_profile_login_no_sn.view.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.databinding.FragmentProfileLoginNoSnBinding
import ru.aleshi.letsplaycities.social.NativeAccess
import ru.aleshi.letsplaycities.utils.Utils
import java.io.File

class LoginNoSnProfileFragment : Fragment() {
    private lateinit var mGamePreferences: GamePreferences
    private lateinit var mProfileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGamePreferences = (requireContext().applicationContext as LPSApplication).gamePreferences
        mProfileViewModel = ViewModelProviders.of(requireActivity())[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentProfileLoginNoSnBinding.inflate(inflater, container, false).apply {
            profile = mProfileViewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateFields(view, mGamePreferences)
        btnEnter.setOnClickListener {
            val input = messageInputField.text.toString()
            if (input.length in 4..20) {
                processAvatar()
                mGamePreferences.updateLastNativeLogin(input)
                NativeAccess.login(input, requireActivity())
                findNavController().popBackStack(R.id.fragmentLoginProfile, true)
            }
        }
        btnCancel.setOnClickListener { findNavController().navigateUp() }
        avatar.setOnClickListener { findNavController().navigate(R.id.showChangeAvatarDialog) }
    }

    private fun processAvatar() {
        mProfileViewModel.avatar.get()?.run {
            val filesDir = requireContext().filesDir
            val bitmap = Observable.just((this as BitmapDrawable).bitmap).share()

            bitmap
                .switchMap { Utils.saveAvatar(filesDir, it) }
                .filter { it.isNotEmpty() }
                .subscribe(mGamePreferences::setAvatarPath)

            bitmap
                .switchMap { Utils.saveAvatar(filesDir, it, "nv") }
                .filter { it.isNotEmpty() }
                .subscribe(mGamePreferences::updateLastAvatarUri)
        }
    }

    private fun populateFields(root: View, prefs: GamePreferences) {
        root.messageInputField.setText(prefs.getLastNativeLogin())
        prefs.getLastAvatarUri()?.run {
            Utils.loadAvatar(File(this).toUri())
                .map { BitmapDrawable(resources, it) }
                .onErrorReturnItem(resources.getDrawable(R.drawable.ic_player) as BitmapDrawable)
                .subscribe(mProfileViewModel.avatar::set)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mGamePreferences.isLoggedFromAnySN())
            mProfileViewModel.loadDefaultAvatar()
    }
}
