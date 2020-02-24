package ru.aleshi.letsplaycities.base.dictionary

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.BadTokenException
import ru.aleshi.letsplaycities.FileProvider
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject

/**
 * Factory interface for creating and loading [DictionaryService] instance
 */
class DictionaryFactory @Inject constructor(
    private val fileProvider: FileProvider
) {

    private val internalPath = File(fileProvider.filesDir, DOWNLOADED_DATA)

    companion object {
        private const val ASSETS_DATA = "data.bin"
        private const val DOWNLOADED_DATA = "data-last.bin"

        private var cachedDictionary: Pair<Int, DictionaryService>? = null
    }

    class DictionaryInfo(val version: Int, val savePath: File)

    /**
     * Loads dictionary either from internal storage or embedded with application
     * @return Newly loaded or cached dictionary
     */
    fun load(): Single<DictionaryService> {
        return Single.fromCallable { parseDictionary(internalPath) }
            .subscribeOn(Schedulers.io())
            .doOnError { internalPath.delete() }
            .retry(1)
    }

    /**
     * Reads latest dictionary (internal or embedded) and return its version with internal
     * save path
     */
    fun getLatestDictionaryInfo(): Single<DictionaryInfo> =
        Single.fromCallable { readVersion(getLatest(internalPath)) }
            .subscribeOn(Schedulers.io())
            .map { DictionaryInfo(it, internalPath) }

    /**
     * Reads dictionary from [internalPath] or [ASSETS_DATA] depending whether the more newer version.
     * @param internalPath location of downloaded dictionary file
     * @return loaded [DictionaryService]
     * @throws BadTokenException if dictionary file is invalid
     */
    private fun parseDictionary(internalPath: File): DictionaryService {
        val inputStream = DataInputStream(getLatest(internalPath))
        val count = inputStream.readInt()
        val version = inputStream.readInt()
        val countTest = inputStream.readInt()
        if (count != countTest shr 12) {
            inputStream.close()
            throw BadTokenException("Invalid file header")
        }

        //Check for existing in cache dictionary with this version
        getFromCacheOrInvalidate(version)?.apply {
            inputStream.close()
            return this
        }

        val dictionary: HashMap<String, DictionaryServiceImpl.CityProperties> = HashMap(count)
        val subDictionary: HashMap<Char, ArrayList<String>> = HashMap()

        for (i in 0 until count + 1) {
            val len = inputStream.readUnsignedByte()
            val sb = StringBuilder(len)
            for (l in 0 until len) {
                sb.append(inputStream.readChar() - 513)
            }
            val name = sb.toString()
            val diff = inputStream.readByte()
            val countryCode = inputStream.readShort()
            val city = DictionaryServiceImpl.CityProperties(diff, countryCode)

            if (i == count) {
                if (Integer.parseInt(name.substring(0, name.length - 6)) != count) {
                    inputStream.close()
                    throw BadTokenException("File checking failed!")
                }
            } else
                dictionary[name] = city

            val f = name[0]
            val list: ArrayList<String>
            if (subDictionary.containsKey(f))
                list = subDictionary[f]!!
            else {
                list = ArrayList()
                subDictionary[f] = list
            }
            list.add(name)
        }
        inputStream.close()

        return DictionaryServiceImpl(
            dictionary,
            subDictionary
        ).apply { saveInCache(version, this) }
    }

    /**
     * Check there is cache with [currentVersion] exists or not.
     * If it exists and doesn't matches with [currentVersion] it will be destroyed.
     * @param currentVersion version for checking cache existence
     * @return already loaded [DictionaryService] with [currentVersion] or `null`
     */
    private fun getFromCacheOrInvalidate(currentVersion: Int): DictionaryService? {
        return cachedDictionary?.run {
            if (first == currentVersion)
                second.apply { reset() }
            else {
                second.clear()
                cachedDictionary = null
                null
            }
        }
    }

    /**
     * Saves or updates cached dictionary instance to [currentVersion] if needed.
     * @param currentVersion version of currently loaded [dictionaryService]
     * @param dictionaryService currently loaded [DictionaryService]
     */
    private fun saveInCache(currentVersion: Int, dictionaryService: DictionaryService) {
        cachedDictionary?.apply {
            if (currentVersion == first)
                return
            else
                second.clear()
        }
        cachedDictionary = currentVersion to dictionaryService
    }

    /**
     * Opens the latest dictionary file of saved in [internal] or embedded [ASSETS_DATA]
     * @param internal location of downloaded dictionary file
     * @return latest present dictionary chosen between [internal] and [ASSETS_DATA]
     */
    private fun getLatest(internal: File): InputStream {
        val intV: Int = internal
            .takeIf { it.exists() }
            ?.run {
                runCatching { readVersion(FileInputStream(internal)) }
                    .onFailure { internal.delete() }
                    .getOrDefault(0)
            }
            ?: 0

        val embV = readVersion(fileProvider.open(ASSETS_DATA))

        return if (intV > embV) FileInputStream(internal) else fileProvider.open(ASSETS_DATA)
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