package ru.aleshi.letsplaycities.ui.game

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.aleshi.letsplaycities.R

class SnackbarDownloadingListener(private val fragment: Fragment) : DictionaryUpdater.DownloadingListener {

    lateinit var snackbar: Snackbar

    override fun onStart() {
        snackbar = Snackbar.make(fragment.requireView(), R.string.loading_dictionary, Snackbar.LENGTH_INDEFINITE)
        snackbar.show()
    }

    override fun onProgress(res: Int) {
        snackbar.setText(fragment.getString(R.string.loading_dictionary) + " $res%")
    }

    override fun onEnd() = showShortSnackbar(R.string.loading_dictionary_completed)

    override fun onError() = showShortSnackbar(R.string.loading_dictionary_error)


    private fun showShortSnackbar(msg: Int) {
        snackbar.duration = Snackbar.LENGTH_SHORT
        snackbar.setText(msg)
        snackbar.show()
    }
}
