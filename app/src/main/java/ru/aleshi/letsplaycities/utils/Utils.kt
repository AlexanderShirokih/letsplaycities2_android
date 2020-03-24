package ru.aleshi.letsplaycities.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.IdRes
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.ThemeManager
import java.io.File
import java.io.FileOutputStream
import java.net.URI

object Utils {

    val Fragment.lpsApplication
        get() = requireContext().applicationContext as LPSApplication

    /**
     * Navigates to [dir] only if current destination is gameFragment.
     */
    fun safeNavigate(navController: NavController, @IdRes dest: Int, dir: NavDirections) {
        if (navController.currentDestination?.id == dest)
            navController.navigate(dir)
    }

    /**
     * Created [Uri] to user image.
     * @param userId userID of user that image URI should be created
     * @param hash image hash of user or `null` if hash not present
     * @return [Uri] containing path to user's picture or [Uri.EMPTY] if [hash] is not present
     * @see getPictureURI
     */
    fun getPictureUri(userId: Int, hash: String?): Uri =
        getPictureURI(userId, hash)?.run { toString().toUri() } ?: Uri.EMPTY

    /**
     * Creates [URI] to user image.
     * @param userId userID of user that image URI should be created
     * @param hash image hash of user or `null` if hash not present
     * @return [URI] containing path to user's picture or `null` if [hash] is not present
     */
    fun getPictureURI(userId: Int, hash: String?): URI? = if (hash == null) null else
        URI.create("${getServerBaseUrl()}user/${userId}/picture?hash=${hash}")

    /**
     * Returns base server URL string
     */
    fun getServerBaseUrl(): String {
        return "http://${BuildConfig.HOST}:8080/"
    }

    /**
     * Loads picture from [uri], then resizes it 128x128px and
     * saves to [filesDir]/img.png
     */
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

    /**
     * Loads image from [uri].
     * @return [Single] with loaded image [Bitmap]
     */
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

    /**
     * Applies theme saved in preferences to the application.
     */
    fun applyTheme(prefs: GamePreferences, context: Context) {
        val theme = ThemeManager.getCurrentTheme(prefs)
        if (theme.isFreeOrAvailable())
            context.setTheme(theme.themeId)
    }

}