package ru.aleshi.letsplaycities.network

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.aleshi.letsplaycities.R
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException

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
            is LPSException -> {
                length = Snackbar.LENGTH_LONG
                message = exception.message!!
            }
            else -> exception.printStackTrace()
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