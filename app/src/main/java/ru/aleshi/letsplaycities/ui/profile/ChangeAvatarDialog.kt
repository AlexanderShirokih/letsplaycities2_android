package ru.aleshi.letsplaycities.ui.profile

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.Utils
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication

class ChangeAvatarDialog : DialogFragment() {

    companion object {
        private const val SELECT_PICTURE = 142
    }

    private var disposable = CompositeDisposable()
    private lateinit var mProfileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProfileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            processAvatar(data!!.data!!)
        }
    }

    private fun processAvatar(uri: Uri) {
        disposable.add(
            Utils.createThumbnail(requireContext().filesDir, uri)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(mProfileViewModel.avatarUri::set)
                .subscribe({
                    lpsApplication.gamePreferences.lastAvatarUri = it.toString()
                    findNavController().popBackStack()
                }
                    , {
                        Toast.makeText(
                            requireContext(),
                            R.string.error_upd_avatar,
                            Toast.LENGTH_LONG
                        ).show()
                    })
        )
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
        disposable.clear()
    }
}