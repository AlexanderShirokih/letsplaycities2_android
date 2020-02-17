package ru.aleshi.letsplaycities.utils

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.dialog_waiting.view.*
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import java.util.concurrent.TimeUnit

object Utils {

    val Fragment.lpsApplication
        get() = requireContext().applicationContext as LPSApplication

    fun getPictureUrl(userId: Int, hash: String?) = if (hash == null) null else
        "${getServerBaseUrl()}user/${userId}/picture?hash=${hash}"

    fun getServerBaseUrl(): String {
        return "http://${BuildConfig.HOST}/"
    }

//    fun resizeAndSave(context: Context, data: Uri): Observable<String> {
//        val filesDir = context.filesDir
//
//        return loadAvatar(data)
//            .onErrorReturnItem((context.resources.getDrawable(R.drawable.ic_player) as BitmapDrawable).bitmap)
//            .switchMap { saveAvatar(filesDir, it) }
//    }

//    fun saveAvatar(
//        filesDir: File,
//        bitmap: Bitmap,
//        saveFileName: String = "0.png"
//    ): Observable<String> {
//        return Observable.just(bitmap)
//            .subscribeOn(Schedulers.io())
//            .map {
//                saveToLocalStorage(filesDir, saveFileName, it)
//            }
//    }

//    fun loadAvatar(src: Uri): Observable<Bitmap> {
//        return Observable.just(src)
//            .subscribeOn(Schedulers.io())
//            .map {
//                Picasso.get()
//                    .load(it)
//                    .networkPolicy(NetworkPolicy.NO_CACHE)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE)
//                    .resize(0, 128)
//                    .get()
//            }
//    }

//    private fun saveToLocalStorage(filesDir: File, saveFileName: String, bitmap: Bitmap): String {
//        val ava = File(filesDir, "avatars")
//        if (!ava.exists()) {
//            ava.mkdir()
//        }
//        return try {
//            val file = File(ava, saveFileName)
//            val fos = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
//            file.absolutePath
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ""
//        }
//
//    }

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