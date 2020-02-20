package ru.aleshi.letsplaycities.utils

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_waiting.view.*
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object Utils {

    val Fragment.lpsApplication
        get() = requireContext().applicationContext as LPSApplication

    fun getPictureUri(userId: Int, hash: String?): Uri? = if (hash == null) null else
        Uri.parse("${getServerBaseUrl()}user/${userId}/picture?hash=${hash}")

    fun getServerBaseUrl(): String {
        return "http://${BuildConfig.HOST}:8080/"
    }

    fun createThumbnail(filesDir: File, uri: Uri): Single<Uri> =
        Single.just(uri)
            .observeOn(Schedulers.computation())
            .flatMap(::loadResized)
            .map {
                val outputFile = File(filesDir, "img.png")
                FileOutputStream(outputFile).apply {
                    it.compress(Bitmap.CompressFormat.PNG, 0, this)
                    close()
                }
                outputFile.toUri()
            }

    private fun loadResized(uri: Uri): Single<Bitmap> =
        Single.create {
            try {
                it.onSuccess(
                    Picasso.get()
                        .load(uri)
                        .resize(0, 128)
                        .get()
                )
            } catch (e: Exception) {
                it.tryOnError(e)
            }
        }

    fun showWaitingForConnectionDialog(
        reconnectionDelay: Int,
        activity: Activity,
        task: () -> Unit,
        cancelCallback: () -> Unit
    ) {
        var active = true
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_waiting, null, false)
        with(AlertDialog.Builder(activity)) {
            setCancelable(true)
            setView(view)
            create()
        }.apply {
            val disposable =
                Observable.intervalRange(0, reconnectionDelay.toLong(), 0, 1, TimeUnit.SECONDS)
                    .map { reconnectionDelay - it }
                    .observeOn(AndroidSchedulers.mainThread())
                    .takeWhile { active }
                    .subscribe(
                        {
                            view.con_waiting_tv.text =
                                activity.getString(R.string.waiting_for_connection, it)
                        },
                        ::error,
                        {
                            dismiss()
                            if (active) task() else cancelCallback()
                        }
                    )
            setOnCancelListener {
                active = false
                cancelCallback()
                disposable.dispose()
            }
        }.show()
    }

}