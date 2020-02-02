package ru.aleshi.letsplaycities.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.databinding.FragmentProfileCardBinding

class ProfileCardFragment : DialogFragment() {

    private lateinit var mGamePreferences: GamePreferences
    private lateinit var mProfileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGamePreferences = (requireContext().applicationContext as LPSApplication).gamePreferences

        mProfileViewModel =
            ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        mProfileViewModel.loadCurrentProfile()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentProfileCardBinding.inflate(inflater, container, false).apply {
            profile = mProfileViewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener { findNavController().navigate(R.id.actionManageProfile) }
    }

}
