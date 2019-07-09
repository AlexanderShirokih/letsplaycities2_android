package ru.aleshi.letsplaycities.ui.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.transition.TransitionInflater
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.utils.Utils
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import java.io.File

class LoginFragment : Fragment() {
    private lateinit var mAvatarModelView: AvatarViewModel
    private lateinit var mApplication: LPSApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)
        mApplication = lpsApplication
        mAvatarModelView = ViewModelProviders.of(requireActivity())[AvatarViewModel::class.java]
        mAvatarModelView.avatarBitmap.observe(this, Observer {
            if (it != null) {
                roundedImageView.setImageBitmap(it)
            } else
                roundedImageView.setImageResource(R.drawable.ic_player)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false).apply {
            val prefs = lpsApplication.gamePreferences
            populateFields(this, prefs)
            btnEnter.setOnClickListener {
                val input = messageInputField.text.toString()
                if (input.length in 4..20) {
                    process()
                    prefs.updateLastNativeLogin(input)
                    mAvatarModelView.nativeLogin.value = input
                    findNavController().navigateUp()
                }
            }
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
            roundedImageView.setOnClickListener { findNavController().navigate(R.id.showChangeAvatarDialog) }
        }
    }

    private fun process() {
        if (mAvatarModelView.avatarBitmap.value != null) {
            val filesDir = requireContext().filesDir
            val bitmap = Observable.just(mAvatarModelView.avatarBitmap.value).share()

            bitmap.switchMap {
                Utils.saveAvatar(filesDir, it)
            }
                .doOnNext {
                    if (it != null) {
                        mApplication.gamePreferences.setAvatarPath(it)
                        mAvatarModelView.avatarPath.postValue(it)
                    }
                }.subscribe()

            bitmap.switchMap {
                Utils.saveAvatar(filesDir, it, "nv")
            }
                .doOnNext {
                    if (it != null) {
                        mApplication.gamePreferences.updateLastAvatarUri(it)
                    }
                }.subscribe()
        }
    }

    private fun populateFields(root: View, prefs: GamePreferences) {
        root.messageInputField.setText(prefs.getLastNativeLogin())
        prefs.getLastAvatarUri()?.run {

            Utils.loadAvatar(File(this).toUri())
                .doOnNext {
                    mAvatarModelView.avatarBitmap.postValue(it)
                }
                .subscribe()
        }
    }
}