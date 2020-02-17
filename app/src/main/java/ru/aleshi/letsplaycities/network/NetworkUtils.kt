package ru.aleshi.letsplaycities.network

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import ru.aleshi.letsplaycities.R
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException

object NetworkUtils {

    fun showErrorSnackbar(
        exception: Throwable,
        fragment: Fragment,
        dismissCallback: (() -> Unit)? = null
    ) {
        val context = fragment.activity!!
        var length = Snackbar.LENGTH_SHORT
        var message = context.getString(R.string.err_msg_on_exception)

        when (exception) {
            is AuthorizationException -> {
                length = Snackbar.LENGTH_LONG
                message = exception.banReason
            }
            is LPSException -> {
                length = Snackbar.LENGTH_LONG
                message = exception.message!!
            }
            else -> exception.printStackTrace()
        }

        Snackbar.make(fragment.requireView(), message, length)
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    dismissCallback?.invoke()
                }
            })
            .show()
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

}