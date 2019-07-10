package ru.aleshi.letsplaycities.ui.friends

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.lpsv3.IErrorListener

class ToasterErrorListener(private val mFragment: Fragment, private val cancelCallback: (() -> Unit)? = null) :
    IErrorListener {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onException(ex: Exception) {
        ex.printStackTrace()
        scope.launch {
            showUiNotification(R.string.err_msg_on_exception)
            cancelCallback?.invoke()
        }
    }

    override fun onInvalidMessage() {
        scope.launch {
            showUiNotification(R.string.err_msg_on_invalid)
            cancelCallback?.invoke()
        }
    }

    private fun showUiNotification(id: Int) {
        Snackbar.make(mFragment.requireView(), id, Snackbar.LENGTH_LONG).show()
    }
}