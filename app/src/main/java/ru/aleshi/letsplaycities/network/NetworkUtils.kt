package ru.aleshi.letsplaycities.network

import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import com.google.android.material.snackbar.Snackbar
import ru.aleshi.letsplaycities.GameException
import ru.aleshi.letsplaycities.R
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException
import java.io.IOException
import java.net.ConnectException

object NetworkUtils {

    fun showErrorSnackbar(
        exception: Throwable,
        fragment: Fragment,
        dismissCallback: (() -> Unit)? = null
    ) {
        val context = fragment.requireActivity()
        var length = Snackbar.LENGTH_SHORT
        var message = context.getString(R.string.err_msg_on_exception)

        when (exception) {
            is AuthorizationException -> {
                length = Snackbar.LENGTH_LONG
                message = exception.banReason
            }
            is LPSException, is GameException -> {
                length = Snackbar.LENGTH_LONG
                message = fragment.getString(R.string.error, exception.message!!)
            }
            is ConnectException -> {
                length = Snackbar.LENGTH_LONG
                message = fragment.getString(R.string.error_server_unavail)
            }
            else -> exception.printStackTrace()
        }

        if (exception !is IOException && exception !is GameException) {
            Crashlytics.logException(exception)
        }

        val view = fragment.view ?: fragment.requireParentFragment().requireView()

        Snackbar.make(view, message, length)
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    dismissCallback?.invoke()
                }
            })
            .show()
    }

}