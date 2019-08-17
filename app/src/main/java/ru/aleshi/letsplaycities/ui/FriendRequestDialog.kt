package ru.aleshi.letsplaycities.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.network.FriendRequestContract
import ru.aleshi.letsplaycities.network.FriendRequestPresenter
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication

class FriendRequestDialog : DialogFragment(), FriendRequestContract.View {

    private val mPresenter: FriendRequestContract.Presenter = FriendRequestPresenter(this)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.request_dialog_title)
            .setMessage(getString(R.string.request_dialog_msg, args.getString("login")))
            .setPositiveButton(R.string.accept) { _, _ ->
                findNavController().navigate(FriendRequestDialogDirections.startNetworkFragment().apply {
                    arguments.apply {
                        putString("action", "fm_game")
                        putInt("oppId", args.getString("user_id")!!.toInt())
                    }
                }
                )
            }
            .setNegativeButton(R.string.decline) { _, _ -> mPresenter.onDecline(args.getString("user_id")!!.toInt()) }
            .create()
    }

    override fun gamePreferences(): GamePreferences = lpsApplication.gamePreferences

    override fun onError(exception: Throwable) = NetworkUtils.handleError(exception, this)

}