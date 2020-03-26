package ru.aleshi.letsplaycities.base.dictionary

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.BadTokenException
import ru.aleshi.letsplaycities.FileProvider
import java.io.*
import javax.inject.Inject

/**
 * Factory interface for creating and loading [DictionaryService] instance
 */
class DictionaryFactory @Inject constructor(
    private val fileProvider: FileProvider,
    private val cache: Cache
) {
    /**
     * Internally keeps instance of [DictionaryService] for later re-usage.
     * @param version version of currently loaded [DictionaryService]
     * @param dictionaryService instance of currently loaded [DictionaryService]
     */
    inner class CacheEntry(var version: Int, var dictionaryService: DictionaryService)

    /**
     * Container for [CacheEntry] which keeps instance of [DictionaryService].
     * @param entry cached instance or null if there are no instances present.
     */
    class Cache(var entry: CacheEntry? = null)

    /**
     * Creates [File] for internal path as filesDir/[internal]
     * @param internal internal file name
     */
    private fun internalPath(internal: String): File = File(fileProvider.filesDir, internal)

    companion object {
        private const val ASSETS_DATA = "data.bin"
        private const val DOWNLOADED_DATA = "data-last.bin"
    }

    class DictionaryInfo(val version: Int, val savePath: File)

    /**
     * Loads dictionary either from internal storage or embedded with application.
     * If an error happens at loading internal dictionary, it will be deleted.
     * If an error happens at loading embedded dictionary, [Single.error] with [BadTokenException]
     * will be raised.
     * @param embedded embedded dictionary file name
     * @param internal internal (downloaded update) file name
     * @return Newly loaded or cached dictionary
     */
    fun load(
        internal: String = DOWNLOADED_DATA,
        embedded: String = ASSETS_DATA
    ): Single<DictionaryService> {
        return Single.fromCallable { parseDictionary(internalPath(internal), embedded) }
            .subscribeOn(Schedulers.io())
            .doOnError { internalPath(internal).delete() }
            .retry(1)
    }

    /**
     * Reads latest dictionary (internal or embedded) and return its version with internal
     * save path
     * @param embedded embedded dictionary file name
     * @param internal internal (downloaded update) file name
     */
    fun getLatestDictionaryInfo(
        internal: String = DOWNLOADED_DATA,
        embedded: String = ASSETS_DATA
    ): Single<DictionaryInfo> =
        Single.fromCallable { readVersion(getLatest(internalPath(internal), embedded)) }
            .subscribeOn(Schedulers.io())
            .map { DictionaryInfo(it, internalPath(internal)) }

    /**
     * Reads dictionary from [internalPath] or [ASSETS_DATA] depending whether the more newer version.
     * @param internalPath location of downloaded dictionary file
     * @param embedded embedded dictionary file name
     * @return loaded [DictionaryService]
     * @throws BadTokenException if dictionary file is invalid
     */
    private fun parseDictionary(internalPath: File, embedded: String): DictionaryService {
        DataInputStream(getLatest(internalPath, embedded)).use { inputStream ->
            val count = inputStream.readInt()
            val version = inputStream.readInt()
            val countTest = inputStream.readInt()

            if (count != countTest shr 12)
                throw BadTokenException("Invalid file header")

            //Check for existing in cache dictionary with this version
            getFromCacheOrInvalidate(version)?.apply {
                return this
            }

            val dictionary: HashMap<String, CityProperties> = HashMap(count)

            for (i in 0 until count + 1) {
                try {
                    val len = inputStream.readUnsignedByte()
                    val sb = StringBuilder(len)
                    for (l in 0 until len) {
                        sb.append(inputStream.readChar() - 513)
                    }
                    val name = sb.toString()
                    val diff = inputStream.readByte()
                    val countryCode = inputStream.readShort()
                    val city = CityProperties(diff, countryCode)

                    if (i == count) {
                        if (Integer.parseInt(name.substring(0, name.length - 6)) != count)
                            throw BadTokenException("File checking failed!")
                    } else
                        dictionary[name] = city
                } catch (e: StringIndexOutOfBoundsException) {
                    throw BadTokenException("File checking failed!")
                } catch (eof: EOFException) {
                    throw BadTokenException("Broken file: EOF")
                }
            }
            return DictionaryServiceImpl(dictionary).apply { saveInCache(version, this) }
        }
    }

    /**
     * Check there is cache with [currentVersion] exists or not.
     * If it exists and doesn't matches with [currentVersion] it will be destroyed.
     * @param currentVersion version for checking cache existence
     * @return already loaded [DictionaryService] with [currentVersion] or `null`
     */
    private fun getFromCacheOrInvalidate(currentVersion: Int): DictionaryService? {
        return cache.entry?.run {
            if (version == currentVersion)
                dictionaryService.apply { reset() }
            else {
                dictionaryService.clear()
                cache.entry = null
                null
            }
        }
    }

    /**
     * Saves cached dictionary instance to [currentVersion] if needed.
     * @param currentVersion version of currently loaded [dictionaryService]
     * @param dictionaryService currently loaded [DictionaryService]
     */
    private fun saveInCache(currentVersion: Int, dictionaryService: DictionaryService) {
        cache.entry = CacheEntry(currentVersion, dictionaryService)
    }

    /**
     * Opens the latest dictionary file of saved in [internal] or embedded [ASSETS_DATA]
     * @param internal location of downloaded dictionary file
     * @param emb embedded dictionary file name
     * @return latest present dictionary chosen between [internal] and [ASSETS_DATA]
     */
    private fun getLatest(internal: File, emb: String): InputStream {
        val intV: Int = internal
            .takeIf { it.exists() }
            ?.run {
                runCatching { readVersion(FileInputStream(internal)) }
                    .onFailure { internal.delete() }
                    .getOrDefault(0)
            }
            ?: 0

        val embV = readVersion(fileProvider.open(emb))

        return if (intV > embV) FileInputStream(internal) else fileProvider.open(emb)
    }

    /**
     * Reads version from header and closes the [inputStream]
     * @param inputStream stream with dictionary file
     * @return version of [inputStream]
     */
    private fun readVersion(inputStream: InputStream): Int {
        return DataInputStream(inputStream).use {
            it.skip(4)
            it.readInt()
        }
    }
}