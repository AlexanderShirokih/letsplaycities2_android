package ru.aleshi.letsplaycities.ui.profile

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.Utils
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication

class ChangeAvatarDialog : DialogFragment() {

    companion object {
        private const val SELECT_PICTURE = 142
    }

    private var disposable: Disposable? = null
    private lateinit var mProfileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProfileViewModel = ViewModelProviders.of(requireActivity())[ProfileViewModel::class.java]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            processData(data!!.data!!)
        }
    }

    private fun processData(data: Uri) {
        disposable = Utils.loadAvatar(data)
            .map { BitmapDrawable(resources, it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate { requireDialog().dismiss() }
            .subscribe(mProfileViewModel.avatar::set)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.select_action)
            .setItems(resources.getStringArray(R.array.avatar_actions), null)
            .create().apply {
                listView.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        when (position) {
                            0 -> pick() // Edit
                            1 -> { // Remove
                                lpsApplication.gamePreferences.removeAvatarPath()
                                mProfileViewModel.loadDefaultAvatar()
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
        startActivityForResult(
            pickPhoto,
            SELECT_PICTURE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}