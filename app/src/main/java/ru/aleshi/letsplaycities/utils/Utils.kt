package ru.aleshi.letsplaycities.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.LPSApplication
import java.io.File
import java.io.FileOutputStream


object Utils {

    val Fragment.lpsApplication
        get() = requireContext().applicationContext as LPSApplication

    fun checkRateDialog(context: Context) {
        //TODO
    }

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

    fun resizeAndSave(context: Context, data: Uri): Observable<String?> {
        val filesDir = context.filesDir

        return loadAvatar(data)
            .switchMap { saveAvatar(filesDir, it) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun saveAvatar(filesDir: File, bitmap: Bitmap): Observable<String?> {
        return Observable.just(bitmap)
            .subscribeOn(Schedulers.io())
            .map {
                saveToLocalStorage(filesDir, it)
            }
    }

    fun loadAvatar(src: Uri): Observable<Bitmap> {
        return Observable.just(src)
            .subscribeOn(Schedulers.io())
            .map {
                Picasso.get()
                    .load(it)
                    .resize(128, 128)
                    .get()
            }
    }

    private fun saveToLocalStorage(filesDir: File, bitmap: Bitmap): String? {
        val ava = File(filesDir, "avatars")
        if (!ava.exists()) {
            ava.mkdir()
        }
        return try {
            val file = File(ava, "0.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}