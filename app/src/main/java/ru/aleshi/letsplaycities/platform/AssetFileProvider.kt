package ru.aleshi.letsplaycities.platform

import android.content.Context
import android.content.res.AssetManager
import ru.aleshi.letsplaycities.FileProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

/**
 * Provides access to asset folder
 */
class AssetFileProvider @Inject constructor(context: Context) : FileProvider {

    private val assetManager: AssetManager = context.assets

    /**
     * Internal storage base files directory
     */
    override val filesDir: File = context.filesDir

    /**
     * Opens file by [fileName]
     * @param fileName the file name
     * @return [InputStream] of open file
     */
    @Throws(IOException::class)
    override fun open(fileName: String): InputStream = assetManager.open(fileName)

}