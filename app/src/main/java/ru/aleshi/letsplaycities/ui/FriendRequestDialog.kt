package ru.aleshi.letsplaycities.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.android.support.AndroidSupportInjection
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.network.FriendRequestViewModel
import javax.inject.Inject

//TODO: Test

class FriendRequestDialog : DialogFragment() {

    private val args: FriendRequestDialogArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var requestViewModel: FriendRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        requestViewModel =
            ViewModelProvider(this, viewModelFactory)[FriendRequestViewModel::class.java]
        requestViewModel.state.observe(this) { state ->
            when (state) {
                FetchState.LoadingState -> onBegin()
                is FetchState.ErrorState -> NetworkUtils.showErrorSnackbar(state.error, this) {
                    dismiss()
                }
                else -> dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val alertDialog = requireDialog() as AlertDialog
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            requestViewModel.onDecline(args.userId)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.request_dialog_title)
            .setMessage(getString(R.string.request_dialog_msg, args.login))
            .setPositiveButton(R.string.accept) { _, _ ->
                findNavController().navigate(
                    FriendRequestDialogDirections.startNetworkFragment(
                        BuildConfig.HOST,
                        "fm_game",
                        args.userId
                    )
                )
            }
            .setNegativeButton(R.string.decline) { _, _ -> }
            .create()
    }

    private fun onBegin() {
        (dialog as AlertDialog?)?.apply {
            setTitle(R.string.sending_result)
            getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            getButton(DialogInterface.BUTTON_NEGATIVE).isEnabled = false
        }
    }

}