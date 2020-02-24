package ru.aleshi.letsplaycities

import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Abstraction layer for managing files
 */
interface FileProvider {

    /**
     * Internal storage base files directory
     */
    val filesDir: File

    /**
     * Opens file by [fileName]
     * @param fileName suddenly, the file name
     * @return [InputStream] of open file
     */
    @Throws(IOException::class)
    fun open(fileName: String): InputStream

}