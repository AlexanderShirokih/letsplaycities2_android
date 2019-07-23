package ru.aleshi.letsplaycities.ui.game

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.databinding.DialogGameResultBinding

class GameResultDialog : DialogFragment() {

    enum class SelectedItem { SHARE, REPLAY, MENU }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val score = GameResultDialogArgs.fromBundle(requireArguments()).score

        return AlertDialog.Builder(activity)
            .setCancelable(false)
            .setView(
                DataBindingUtil.inflate<DialogGameResultBinding>(
                    LayoutInflater.from(activity),
                    R.layout.dialog_game_result,
                    null,
                    false
                ).apply {
                    result = GameResultDialogArgs.fromBundle(requireArguments()).result +
                            if (score > 0) activity.getString(R.string.your_score, score) else ""
                    shareButtonVisible = score != -1
                    fragment = this@GameResultDialog
                }.root
            )
            .create()
    }

    fun onClick(item: SelectedItem) {
        requireDialog().dismiss()
        when (item) {
            SelectedItem.MENU -> findNavController().popBackStack(
                R.id.mainMenuFragment,
                false
            )
            SelectedItem.SHARE -> startShareIntent(GameResultDialogArgs.fromBundle(requireArguments()).score)
            SelectedItem.REPLAY -> {
                val nav = findNavController()
                if (!nav.popBackStack(R.id.networkFragment, false))
                    nav.navigate(R.id.start_game_fragment)
            }
        }
    }

    private fun startShareIntent(score: Int) {
        val c = requireContext()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, c.getString(R.string.share_msg, score, c.getString(R.string.app_name)))
        }
        val chooser = Intent.createChooser(intent, c.getString(R.string.share_with))
        chooser.resolveActivity(c.packageManager)?.let {
            startActivity(chooser)
        }
    }
}