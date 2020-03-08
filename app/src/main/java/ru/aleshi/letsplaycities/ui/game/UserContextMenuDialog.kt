package ru.aleshi.letsplaycities.ui.game

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import ru.aleshi.letsplaycities.R

class UserContextMenuDialog : DialogFragment() {

    private lateinit var userMenuViewModel: UserMenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userMenuViewModel =
            ViewModelProvider(requireParentFragment())[UserMenuViewModel::class.java]
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
                    1 -> if (!args.isFriend) sendFriendRequest(args.userId) else Toast.makeText(
                        requireContext(),
                        R.string.already_friend,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .create()
    }


    /**
     * Sends friend request to [userId].
     * @param userId id of user that we want to add to friends
     */
    private fun sendFriendRequest(userId: Int) =
        userMenuViewModel.sendFriendRequest(userId)

    /**
     * Sends ban message to [userId]. After successful sending this message game will be stopped
     * and pop back parent view.
     */
    private fun banPlayer(login: String, userId: Int) =
        userMenuViewModel.banUser(userId, login)

}