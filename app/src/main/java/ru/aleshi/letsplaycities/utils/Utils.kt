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
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_waiting.view.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import java.io.File
import java.io.FileOutputStream
import java.util.*


object Utils {
    const val RECONNECTION_DELAY_MS = 5000

    val Fragment.lpsApplication
        get() = requireContext().applicationContext as LPSApplication

    fun resizeAndSave(context: Context, data: Uri): Observable<String?> {
        val filesDir = context.filesDir

        return loadAvatar(data)
            .switchMap { saveAvatar(filesDir, it) }
    }

    fun saveAvatar(filesDir: File, bitmap: Bitmap, saveFileName: String = "0.png"): Observable<String?> {
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
                    .resize(128, 128)
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

    private fun saveToLocalStorage(filesDir: File, saveFileName: String, bitmap: Bitmap): String? {
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
            null
        }

    }

    fun showWaitingForConnectionDialog(activity: Activity, task: () -> Unit, cancelCallback: () -> Unit) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_waiting, null, false)
        with(AlertDialog.Builder(activity)) {
            setCancelable(true)
            setView(view)
            create()
        }.apply {
            val timeBegin = System.currentTimeMillis()
            val tt = Timer()
            tt.schedule(object : TimerTask() {
                override fun run() {
                    val t = (RECONNECTION_DELAY_MS - (System.currentTimeMillis() - timeBegin)) / 1000
                    if (t <= 0) {
                        tt.cancel()
                        dismiss()
                        task()
                    } else
                        activity.runOnUiThread {
                            view.con_waiting_tv.text =
                                activity.getString(R.string.waiting_for_connection, t.toString())
                        }
                }
            }, 0, 500)
            setOnCancelListener {
                tt.cancel()
                cancelCallback()
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