package ru.aleshi.letsplaycities.ui.confirmdialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import ru.aleshi.letsplaycities.R

class ConfirmationDialog : DialogFragment() {

    private lateinit var confirmViewModel: ConfirmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        confirmViewModel = ViewModelProvider(requireActivity())[ConfirmViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = ConfirmationDialogArgs.fromBundle(requireArguments())
        return AlertDialog.Builder(requireActivity())
            .setTitle(args.title)
            .setMessage(args.message)
            .setPositiveButton(R.string.yes) { _, _ ->
                dispatchResult(args.requestCode, true)
            }
            .setNegativeButton(R.string.no) { _, _ ->
                dispatchResult(args.requestCode, false)
            }
            .create()
    }

    private fun dispatchResult(requestCode: Int, res: Boolean) {
        confirmViewModel.callback.postValue(ConfirmViewModel.Request(requestCode, res))
    }

}