package ru.aleshi.letsplaycities.ui.global

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import dagger.android.support.AndroidSupportInjection
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.FetchState
import javax.inject.Inject

class FriendRequestDialog : DialogFragment() {

    private val args: FriendRequestDialogArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var gameRequestViewModel: FriendRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        gameRequestViewModel =
            ViewModelProvider(this, viewModelFactory)[FriendRequestViewModel::class.java]
        gameRequestViewModel.state.observe(this) { state ->
            when (state) {
                FetchState.LoadingState -> onBegin()
                is FetchState.ErrorState -> {
                    NetworkUtils.showErrorSnackbar(state.error, this)
                    dismiss()
                }
                else -> dismiss()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.friend_request_title)
            .setMessage(getString(R.string.friend_request_message, args.login))
            .setPositiveButton(R.string.accept) { _, _ -> }
            .setNegativeButton(R.string.decline) { _, _ -> }
            .create()
    }

    override fun onStart() {
        super.onStart()
        val alertDialog = requireDialog() as AlertDialog
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            gameRequestViewModel.sendResult(args.userId, false)
        }
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            gameRequestViewModel.sendResult(args.userId, true)
        }
    }

    private fun onBegin() {
        (dialog as AlertDialog?)?.apply {
            setTitle(R.string.sending_result)
            getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            getButton(DialogInterface.BUTTON_NEGATIVE).isEnabled = false
        }
    }

}