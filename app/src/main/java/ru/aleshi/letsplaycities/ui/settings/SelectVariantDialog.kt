package ru.aleshi.letsplaycities.ui.settings

import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SelectVariantDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = SelectVariantDialogArgs.fromBundle(requireArguments())
        val items = resources.getStringArray(args.items)

        val builder = MaterialAlertDialogBuilder(requireActivity())
            .setTitle(args.title)
            .setNegativeButton(android.R.string.cancel, null)
        if (items.size > 3) {
            val picker = NumberPicker(requireActivity()).apply {
                minValue = 0
                displayedValues = items
                maxValue = items.size - 1
                value = args.currentItem
            }
            builder.setView(picker)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                postSelectedItem(args.itemPosition, picker.value)
            }
        } else {
            builder.setSingleChoiceItems(items, args.currentItem) { dialog, id ->
                postSelectedItem(args.itemPosition, id)
                dialog.dismiss()
            }
        }
        return builder.create()
    }

    private fun postSelectedItem(position: Int, value: Int) {
        ViewModelProvider(requireActivity())[SettingsViewModel::class.java].selectedItem.value =
            position to value
    }
}