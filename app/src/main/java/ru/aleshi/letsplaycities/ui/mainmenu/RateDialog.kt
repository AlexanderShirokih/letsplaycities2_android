package ru.aleshi.letsplaycities.ui.mainmenu

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.aleshi.letsplaycities.R

class RateDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setMessage(R.string.rate_dialog)
            .setCancelable(false)
            .setPositiveButton(R.string.rate) { _, _ ->
                val context = requireContext()
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${context.packageName}")
                    )
                )
            }
            .setNegativeButton(R.string.no_thanks) { _, _ -> }
            .create()
    }
}