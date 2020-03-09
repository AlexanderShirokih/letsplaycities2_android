package ru.aleshi.letsplaycities.ui.game

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.databinding.DialogGameResultBinding

/**
 * Dialog that presents game results.
 * Has three options:
 * - Share game results
 * - Replay
 * - Go to menu
 */
class GameResultDialog : DialogFragment() {

    private val args: GameResultDialogArgs by navArgs()

    /**
     * Identifies game buttons
     */
    enum class SelectedItem { SHARE, REPLAY, MENU }

    /**
     * Called when dialog starts
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val score = args.score

        return AlertDialog.Builder(activity)
            .setCancelable(false)
            .setView(
                DataBindingUtil.inflate<DialogGameResultBinding>(
                    LayoutInflater.from(activity),
                    R.layout.dialog_game_result,
                    null,
                    false
                ).apply {
                    result = args.result +
                            if (score > 0) activity.getString(R.string.your_score, score) else ""
                    shareButtonVisible = score != -1
                    fragment = this@GameResultDialog
                }.root
            )
            .create().apply { setCanceledOnTouchOutside(false) }
    }

    /**
     * Called by view binding when button clicked.
     * @param item button that was clicked
     */
    fun onClick(item: SelectedItem) {
        requireDialog().dismiss()
        when (item) {
            SelectedItem.MENU -> findNavController().popBackStack(
                R.id.mainMenuFragment,
                false
            )
            SelectedItem.SHARE -> startShareIntent(args.score)
            SelectedItem.REPLAY -> {
                val nav = findNavController()
                if (!nav.popBackStack(R.id.networkFragment, false)) {
                    nav.popBackStack(R.id.gameFragment, true)
                    nav.navigate(R.id.start_game_fragment)
                }
            }
        }
    }

    /**
     * Creates and starts share action
     */
    private fun startShareIntent(score: Int) {
        val c = requireContext()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                c.getString(R.string.share_msg, score, c.getString(R.string.app_name))
            )
        }
        val chooser = Intent.createChooser(intent, c.getString(R.string.share_with))
        chooser.resolveActivity(c.packageManager)?.let {
            startActivity(chooser)
        }
    }
}