package ru.aleshi.letsplaycities.network

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.lpsv3.AuthorizationException
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient2
import ru.aleshi.letsplaycities.network.lpsv3.NetworkRepository

object NetworkUtils {

    fun updateToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(NetworkUtils::class.java.simpleName, "FirebaseInstanceId.getInstance() failed", task.exception)
            } else {
                //174 chars
                NetworkRepository(NetworkClient2()).apply {
                    sendFirebaseToken(task.result!!.token)
                    disconnect()
                }
            }
        }
    }

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
            Snackbar.make(
                fragment.requireView(),
                context.getString(R.string.err_msg_on_exception), Snackbar.LENGTH_SHORT
            )
                .show()
        }
    }

}