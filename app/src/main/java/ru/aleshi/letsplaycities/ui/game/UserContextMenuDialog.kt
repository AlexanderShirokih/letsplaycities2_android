package ru.aleshi.letsplaycities.ui.game

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.NetworkUtils.handleError

class UserContextMenuDialog : DialogFragment() {

    private lateinit var gameSessionViewModel: GameSessionViewModel

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameSessionViewModel =
            ViewModelProviders.of(requireActivity())[GameSessionViewModel::class.java]
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

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun sendFriendRequest() {
        gameSessionViewModel.gameSession?.let {
            val activity = requireActivity()
            it.sendFriendRequest()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(activity, R.string.new_friend_request, Toast.LENGTH_LONG).show()
                }, { err -> handleError(err, this) })
        }
    }

    private fun banPlayer(login: String, userId: Int) {
        val activity = requireActivity()
        gameSessionViewModel.gameSession
            ?.banUser(userId)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doAfterTerminate { findNavController().popBackStack(R.id.gameFragment, true) }
            ?.subscribe(
                {
                    Toast.makeText(
                        activity,
                        getString(R.string.user_banned, login),
                        Toast.LENGTH_SHORT
                    ).show()
                }, { t -> handleError(t, this) }
            )
            ?.addTo(disposable)
    }
}