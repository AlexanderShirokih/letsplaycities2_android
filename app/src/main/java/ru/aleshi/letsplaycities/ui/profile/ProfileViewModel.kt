package ru.aleshi.letsplaycities.ui.profile

import android.app.Application
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.net.toUri
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.AuthType
import java.io.File

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private var disposable: Disposable? = null

    val avatar: ObservableField<Drawable> = ObservableField()

    val login: ObservableField<String> = ObservableField()

    val authType: ObservableField<Drawable> = ObservableField()

    init {
        loadDefaultAvatar()
    }

    fun loadCurrentProfile() {
        val context = getApplication<LPSApplication>()
        val prefs = context.gamePreferences
        if (prefs.isLoggedFromAnySN()) {
            val authData = GameAuthDataFactory().loadFromPreferences(prefs)
            login.set(authData.login)
            authType.set(context.getDrawableFromResource(getAuthResourceByAuthType(authData.snType)))
            val avatar = prefs.getAvatarPath()
            if (avatar == null) {
                loadDefaultAvatar()
            } else {
                disposable = Utils.loadAvatar(File(avatar).toUri())
                    .map { BitmapDrawable(context.resources, it) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this.avatar::set)
            }
        } else {
            login.set(context.getString(R.string.profile_not_authorized))
            authType.set(context.getDrawableFromResource(getAuthResourceByAuthType(AuthType.Native)))
            loadDefaultAvatar()
        }
    }

    fun loadDefaultAvatar() {
        avatar.set(getApplication<LPSApplication>().getDrawableFromResource(R.drawable.ic_player))
    }

    private fun getAuthResourceByAuthType(type: AuthType): Int {
        return when (type) {
            AuthType.Native -> android.R.color.transparent
            AuthType.Google -> R.drawable.ic_gl
            AuthType.Vkontakte -> R.drawable.ic_vk
            AuthType.Odnoklassniki -> R.drawable.ic_ok
            AuthType.Facebook -> R.drawable.ic_fb
        }
    }

    private fun Context.getDrawableFromResource(resId: Int): Drawable {
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resources.getDrawable(resId, theme)
        } else
            resources.getDrawable(resId)
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

}