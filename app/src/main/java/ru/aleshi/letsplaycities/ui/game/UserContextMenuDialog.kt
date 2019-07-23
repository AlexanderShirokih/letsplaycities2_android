package ru.aleshi.letsplaycities.ui.game

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication

class UserContextMenuDialog : DialogFragment() {

    private lateinit var gameSessionViewModel: GameSessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameSessionViewModel = ViewModelProviders.of(requireActivity())[GameSessionViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = UserContextMenuDialogArgs.fromBundle(requireArguments())
        return AlertDialog.Builder(requireActivity())
            .setTitle(args.name)
            .setItems(R.array.player_menu) { _, i ->
                when (i) {
                    // Ban
                    0 -> banPlayer(args.name, args.userId)
                    // Friends
                    1 -> if (!args.isFriend) sendFriendRequest() else Toast.makeText(
                        requireContext(),
                        R.string.already_friend,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .create()
    }

    private fun sendFriendRequest() {
        gameSessionViewModel.gameSession.value?.let {
            it.sendFriendRequest()
            Toast.makeText(requireActivity(), R.string.new_friend_request, Toast.LENGTH_LONG).show()
        }
    }

    private fun banPlayer(login: String, userId: Int) {
        lpsApplication.banManager.addToBanList(login, userId)
        Toast.makeText(requireActivity(), getString(R.string.user_banned, login), Toast.LENGTH_SHORT).show()
        findNavController().popBackStack(R.id.gameFragment, true)
    }
}