package ru.aleshi.letsplaycities2.ui.confirmdialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import ru.aleshi.letsplaycities2.R

class ConfirmationDialog : DialogFragment() {

    private lateinit var confirmViewModel: ConfirmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        confirmViewModel = ViewModelProviders.of(requireActivity())[ConfirmViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setMessage(ConfirmationDialogArgs.fromBundle(requireArguments()).message)
            .setPositiveButton(R.string.yes) { _, _ ->
                dispatchResult(true)
            }
            .setNegativeButton(R.string.no) { _, _ ->
                dispatchResult(false)
            }
            .create()
    }

    private fun dispatchResult(res: Boolean) {
        confirmViewModel.callback.postValue(res)
    }

}