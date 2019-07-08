package ru.aleshi.letsplaycities.social

import android.content.Context
import android.net.Uri
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.utils.Utils

object SocialUtils {

    fun saveAvatar(context: Context, src: Uri, listener: () -> Unit) {
        val app = context.applicationContext as LPSApplication
        Utils.resizeAndSave(context, src)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (it != null)
                    app.gamePreferences.setAvatarPath(it)
                listener()
            }
            .subscribe()
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