package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.network.NetworkViewModel
import ru.aleshi.letsplaycities.utils.Utils

object SocialUtils {

    fun saveAvatar(activity: Activity, src: Uri, listener: () -> Unit) {
        updateAvatar(activity as MainActivity, src)
            .doOnNext {
                listener()
            }
            .subscribe()
    }

    fun updateAvatar(activity: MainActivity, src: Uri): Observable<String?> {
        val app = activity.applicationContext as LPSApplication

        return Utils.resizeAndSave(activity, src)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (it != null) {
                    app.gamePreferences.setAvatarPath(it)
                    ViewModelProviders.of(activity)[NetworkViewModel::class.java].avatarPath.value = it
                }
            }
    }

    fun md5(src: String): String {
        val md = java.security.MessageDigest.getInstance("MD5")
        val array = md.digest(src.toByteArray(charset("UTF-8")))
        val sb = StringBuilder()
        for (b in array) {
            sb.append(Integer.toHexString(b.toInt() and 0xFF or 0x100).substring(1, 3))
        }
        return sb.toString()
    }
}