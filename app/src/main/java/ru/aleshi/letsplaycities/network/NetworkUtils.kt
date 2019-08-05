package ru.aleshi.letsplaycities.network

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import ru.aleshi.letsplaycities.R
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException

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

    fun getToken() : Single<String> {
        return Single.create {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                //174 chars
                if (task.isSuccessful)
                    it.onSuccess(task.result!!.token)
                else it.onError(LPSException("Cannot get firebase token"))
            }
        }
    }
}