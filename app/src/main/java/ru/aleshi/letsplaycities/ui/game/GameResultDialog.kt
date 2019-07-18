package ru.aleshi.letsplaycities.ui.game

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.databinding.DialogGameResultBinding

class GameResultDialog : DialogFragment() {

    lateinit var gameResultViewModel: GameResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameResultViewModel = ViewModelProviders.of(requireActivity())[GameResultViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        return AlertDialog.Builder(activity)
            .setCancelable(false)
            .setView(
                DataBindingUtil.inflate<DialogGameResultBinding>(
                    LayoutInflater.from(activity),
                    R.layout.dialog_game_result,
                    null,
                    false
                ).apply {
                    result = GameResultDialogArgs.fromBundle(requireArguments()).result
                    fragment = this@GameResultDialog
                }.root
            )
            .create()
    }

    fun onClick(item: GameResultViewModel.SelectedItem) {
        requireDialog().dismiss()
        gameResultViewModel.result.value = item
    }
}