package ru.aleshi.letsplaycities.ui.profile

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.utils.Event
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.AuthType

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private var disposable: Disposable? = null

    val avatarUri: ObservableField<Uri> = ObservableField()

    val login: ObservableField<String> = ObservableField()

    val authType: ObservableField<Drawable> = ObservableField()

    val nativeEvents =  MutableLiveData<Event<Unit>>()

    init {
        loadDefaultAvatar()
    }

    fun updatePictureHash(userId: Int, newHash: String?) {
        avatarUri.set(Utils.getPictureUri(userId, newHash))
        getApplication<LPSApplication>().gamePreferences.pictureHash = newHash

    }

    fun loadCurrentProfile() {
        val context = getApplication<LPSApplication>()
        val authData = GameAuthDataFactory(context).load()
        if (authData.credentials.isValid()) {
            login.set(authData.login)
            authType.set(context.getDrawableFromResource(getAuthResourceByAuthType(authData.snType)))
            updatePictureHash(authData.credentials.userId, context.gamePreferences.pictureHash)
        } else {
            login.set(context.getString(R.string.profile_not_authorized))
            authType.set(context.getDrawableFromResource(getAuthResourceByAuthType(AuthType.Native)))
            loadDefaultAvatar()
        }
    }

    fun loadDefaultAvatar() {
        avatarUri.set(Uri.EMPTY)
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