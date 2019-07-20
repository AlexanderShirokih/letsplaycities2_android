package ru.aleshi.letsplaycities.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
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

    fun firstToUpper(input: String): String {
        val sb = StringBuilder()
        for (i in 0 until input.length) {
            var c = input[i]
            if (i == 0 || input[i - 1] == '-' || input[i - 1] == ' ')
                c = Character.toUpperCase(c)
            sb.append(c)
        }
        return sb.toString()
    }

    fun formatName(name: String): String {
        val ind = name.indexOf(" ")
        return if (ind > 0) "${name.substring(0, ind)}а${name.substring(ind)}" else "${name}а"
    }


    fun formatCity(city: String): String {
        val s = city.trim().toLowerCase()
        val replaced = s.replace(" ", "-").replace("ё", "е")
        val sb = StringBuilder()
        var prev: Char = 0.toChar()
        for (i in 0 until replaced.length) {
            val c = replaced[i]

            if (!(c == '-' && prev == '-')) {
                sb.append(c)
            }
            prev = c
        }
        return sb.toString()
    }


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

    fun loadDrawable(context: Context, resId: Int): Drawable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.resources.getDrawable(resId, context.theme)
        } else {
            context.resources.getDrawable(resId)
        }
    }

    fun findLastSuitableChar(city: String): Char? {
        return city.reversed().toCharArray()
            .find { it != 'ь' && it != 'ъ' && it != 'ы' && it != 'ё' }
    }

}