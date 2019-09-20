package ru.aleshi.letsplaycities.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_waiting.view.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


object Utils {

    val Fragment.lpsApplication
        get() = requireContext().applicationContext as LPSApplication

    fun resizeAndSave(context: Context, data: Uri): Observable<String> {
        val filesDir = context.filesDir

        return loadAvatar(data)
            .onErrorReturnItem((context.resources.getDrawable(R.drawable.ic_player) as BitmapDrawable).bitmap)
            .switchMap { saveAvatar(filesDir, it) }
    }

    fun saveAvatar(filesDir: File, bitmap: Bitmap, saveFileName: String = "0.png"): Observable<String> {
        return Observable.just(bitmap)
            .subscribeOn(Schedulers.io())
            .map {
                saveToLocalStorage(filesDir, saveFileName, it)
            }
    }

    fun loadAvatar(context: Context, byteArray: ByteArray?, placeholder: Int): Maybe<Drawable> {
        return if (byteArray == null)
            Maybe.just(loadDrawable(context, placeholder))
        else
            Maybe.just(byteArray)
                .subscribeOn(Schedulers.computation())
                .map {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    if (bitmap == null)
                        loadDrawable(context, placeholder)
                    else
                        BitmapDrawable(context.resources, bitmap)
                }
    }

    fun loadAvatar(src: Uri): Observable<Bitmap> {
        return Observable.just(src)
            .subscribeOn(Schedulers.io())
            .map {
                Picasso.get()
                    .load(it)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resize(0, 128)
                    .get()
            }
    }

    fun loadDrawable(context: Context, resId: Int): Drawable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.resources.getDrawable(resId, context.theme)
        } else {
            context.resources.getDrawable(resId)
        }
    }

    private fun saveToLocalStorage(filesDir: File, saveFileName: String, bitmap: Bitmap): String {
        val ava = File(filesDir, "avatars")
        if (!ava.exists()) {
            ava.mkdir()
        }
        return try {
            val file = File(ava, saveFileName)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
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
            val disposable = Observable.intervalRange(0, reconnectionDelay.toLong(), 0, 1, TimeUnit.SECONDS)
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

    fun makeConfirmDialog(activity: Activity, msg: String, onResult: (result: Boolean) -> Unit) {
        AlertDialog.Builder(activity)
            .setMessage(msg)
            .setPositiveButton(R.string.yes) { _, _ ->
                onResult(true)
            }
            .setNegativeButton(R.string.no) { _, _ ->
                onResult(false)
            }
            .create().show()
    }

}