package ru.aleshi.letsplaycities.ui.network

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.Utils
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication

class ChangeAvatarDialog : DialogFragment() {

    companion object {
        private const val SELECT_PICTURE = 142
    }

    private lateinit var mAvatarViewModel: AvatarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAvatarViewModel = ViewModelProviders.of(requireActivity())[AvatarViewModel::class.java]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            val app = lpsApplication
            Utils.resizeAndSave(app, data!!.data!!)
                .doOnNext {
                    if (it != null) {
                        app.gamePreferences.setAvatarPath(it)
                        mAvatarViewModel.avatarPath.postValue(it)
                    }
                }
                .subscribe()

            requireDialog().dismiss()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.select_action)
            .setItems(resources.getStringArray(R.array.avatar_actions), null)
            .create().apply {
                listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    when (position) {
                        //Edit
                        0 -> pick()
                        //Remove
                        1 -> {
                            lpsApplication.gamePreferences.removeAvatarPath()
                            mAvatarViewModel.avatarPath.value = null
                            requireDialog().dismiss()
                        }
                    }
                }
            }
    }

    private fun pick() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickPhoto, SELECT_PICTURE)
    }
}