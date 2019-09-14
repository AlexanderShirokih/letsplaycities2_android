package ru.aleshi.letsplaycities.network

import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.player.GamePlayerDataFactory
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.PlayerData
import java.io.ByteArrayOutputStream
import java.io.File

object NetworkUtils {

    fun handleError(exception: Throwable, fragment: Fragment) {
        val context = fragment.activity!!
        if (exception is AuthorizationException) {
            if (exception.banReason != null) {
                Toast.makeText(context, exception.banReason, Toast.LENGTH_LONG).show()
            } else {
                exception.printStackTrace()
                val err = exception.connectionError ?: "error #04"
                Snackbar.make(
                    fragment.requireView(),
                    context.getString(R.string.server_auth_error, err),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            exception.printStackTrace()
            Snackbar.make(
                fragment.requireView(),
                context.getString(R.string.err_msg_on_exception), Snackbar.LENGTH_SHORT
            )
                .show()
        }
    }

    fun getToken(): Single<String> {
        return Single.create {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                //174 chars
                if (task.isSuccessful)
                    it.onSuccess(task.result!!.token)
                else it.onError(LPSException("Cannot get firebase token"))
            }
        }
    }

    fun createPlayerData(
        versionInfo: Pair<String, Int>,
        callback: (playerData: PlayerData) -> Unit,
        prefs: GamePreferences,
        mGamePlayerDataFactory: GamePlayerDataFactory,
        mAuthData: AuthData
    ) {
        val userData = mGamePlayerDataFactory.create(mAuthData).apply {
            clientVersion = versionInfo.first
            clientBuild = versionInfo.second
            canReceiveMessages = prefs.canReceiveMessages()
        }

        val path = prefs.getAvatarPath()
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                Utils.loadAvatar(file.toUri())
                    .doOnNext { bitmap ->
                        ByteArrayOutputStream().apply {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, this)
                            userData.avatar = toByteArray()
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        callback(userData)
                    }
                    .subscribe()
            } else
                callback(userData)
        } else callback(userData)
    }
}