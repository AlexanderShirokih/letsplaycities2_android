package ru.aleshi.letsplaycities.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.squareup.picasso.Picasso
import ru.aleshi.letsplaycities.R

class PreviewImageDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = PreviewImageDialogArgs.fromBundle(requireArguments())
        return AlertDialog.Builder(requireActivity())
            .setTitle(args.title)
            .setView(ImageView(requireActivity()).apply {
                Picasso.get().isLoggingEnabled = true
                Picasso.get()
                    .load("file:///android_asset/${args.assetImageName}".toUri())
                    .resizeDimen(R.dimen.previewImageWidth, R.dimen.previewImageHeight)
                    .into(this)
            })
            .create()
    }
}