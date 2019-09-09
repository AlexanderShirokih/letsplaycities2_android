package ru.aleshi.letsplaycities.social

import android.content.Context
import android.net.Uri
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.utils.Utils

object SocialUtils {

    fun saveAvatar(context: Context, src: Uri, listener: () -> Unit) {
        updateAvatar(context, src)
            .doOnNext {
                listener()
            }
            .subscribe()
    }

    fun updateAvatar(activity: Context, src: Uri): Observable<String> {
        val app = activity.applicationContext as LPSApplication

        return Utils.resizeAndSave(activity, src)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (it != null) {
                    app.gamePreferences.setAvatarPath(it)
                }
            }
    }
}