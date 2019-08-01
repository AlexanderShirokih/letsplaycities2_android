package ru.aleshi.letsplaycities.ui.game

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.dictionary.DictionaryUpdater

class SnackbarDownloadingListener(private val fragment: Fragment) : DictionaryUpdater.DownloadingListener {

    private var snackbar: Snackbar? = null
    private var hasStarted = false

    override fun onStart() {
        hasStarted = true
        fragment.view?.let {
            snackbar = Snackbar.make(it, R.string.loading_dictionary, Snackbar.LENGTH_INDEFINITE)
            snackbar!!.show()
        }
    }

    override fun onProgress(res: Int) {
        snackbar?.setText(fragment.getString(R.string.loading_dictionary) + " $res%")
    }

    override fun onEnd() {
        if (hasStarted)
            showShortSnackbar(R.string.loading_dictionary_completed)
    }

    override fun onError() = showShortSnackbar(R.string.loading_dictionary_error)


    private fun showShortSnackbar(msg: Int) {
        snackbar?.run {
            duration = Snackbar.LENGTH_SHORT
            setText(msg)
            show()
        }
    }
}
