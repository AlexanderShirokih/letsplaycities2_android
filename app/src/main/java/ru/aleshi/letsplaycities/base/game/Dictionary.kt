package ru.aleshi.letsplaycities.base.game

import android.content.Context
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.BadTokenException
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class Dictionary private constructor(
    private val mDictionary: HashMap<String, CityProperties>,
    private val mSubDictionary: HashMap<Char, ArrayList<String>>,
    private val mExclusions: Exclusions,
    val version: Int
) {

    var difficulty: Byte = 1

    companion object Factory {
        private const val ASSETS_DATA = "data.bin"
        private const val DOWNLOADED_DATA = "data-last.bin"

        fun load(context: Context, exclusions: Exclusions): Single<Dictionary> {
            return Single.just(context)
                .subscribeOn(Schedulers.computation())
                .map { parseDictionary(it, exclusions) }
        }

        private fun parseDictionary(context: Context, exclusions: Exclusions): Dictionary {
            val inputStream = DataInputStream(
                openInputStream(
                    context
                )
            )
            val count = inputStream.readInt()
            val version = inputStream.readInt()
            val countTest = inputStream.readInt()
            if (count != countTest shr 12)
                throw BadTokenException()

            val dictionary: HashMap<String, CityProperties> = HashMap(count)
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
                val city = CityProperties(diff, countryCode)

                if (i == count) {
                    if (Integer.parseInt(name.substring(0, name.length - 6)) != count) {
                        inputStream.close()
                        throw BadTokenException()
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
            return Dictionary(dictionary, subDictionary, exclusions, version)
        }

        private fun openInputStream(context: Context): InputStream {
            val internal = File(context.filesDir, DOWNLOADED_DATA)
            return if (internal.exists())
                FileInputStream(internal)
            else
                context.assets.open(ASSETS_DATA)
        }
    }

    fun applyCity(city: String): Pair<String, CityResult> {
        return if (!mDictionary.containsKey(city)) {
            city to CityResult.CITY_NOT_FOUND
        } else {
            val prop = mDictionary[city]!!
            if (prop.diff < 0)
                city to CityResult.ALREADY_USED
            else {
                prop.flipUsageFlag()
                city to CityResult.OK
            }
        }
    }

    fun getCountryCode(city: String): Short {
        mDictionary[city]?.run {
            return countryCode
        }
        return 0
    }

    fun getRandomWord(first: Char, help: Boolean): Maybe<String> {
        return Maybe.just(first)
            .subscribeOn(Schedulers.computation())
            .filter { mSubDictionary.containsKey(it) }
            .flatMap {
                val ready = ArrayList<String>()
                val list = mSubDictionary[it]!!
                for (s in list) {
                    val cp = mDictionary[s]!!
                    val b = cp.diff
                    if (b > 0 && cp.countryCode != 0.toShort() && (help || b == difficulty) && mExclusions.hasNoExclusions(
                            s
                        )
                    )
                        ready.add(s)
                }
                if (ready.isEmpty()) {
                    Maybe.empty<String>()
                }
                val word = ready[(0 until ready.size).random()]
                mDictionary[word]!!.flipUsageFlag()
                ready.clear()
                Maybe.just(word)
            }
    }

    fun getCorrectionVariants(word: String): List<String> {
        val list = edits(word)
        val candidates = ArrayList<String>()
        for (s in list) {
            // Max 3 words
            if (candidates.size == 3)
                break
            if (canUse(s))
                candidates.add(s)
        }

        if (candidates.isNotEmpty())
            return candidates

        for (s in list)
            for (w in edits(s))
                if (candidates.size < 4 && canUse(w) && !candidates.contains(w))
                    candidates.add(w)
        return if (candidates.isNotEmpty()) candidates else emptyList()
    }


    private fun edits(word: String): ArrayList<String> {
        val result = ArrayList<String>()
        val firstChar = word[0]
        var s: String

        for (i in 0 until word.length) {
            s = word.substring(0, i) + word.substring(i + 1)
            if (s.isNotEmpty() && s[0] == firstChar)
                result.add(s)
        }
        for (i in 0 until word.length - 1) {
            s = (word.substring(0, i) + word.substring(i + 1, i + 2) + word.substring(i, i + 1)
                    + word.substring(i + 2))
            if (s.isNotEmpty() && s[0] == firstChar)
                result.add(s)
        }
        for (i in 0 until word.length) {
            for (c in 'а' until 'я') {
                s = word.substring(0, i) + c.toString() + word.substring(i + 1)
                if (s.isNotEmpty() && s[0] == firstChar)
                    result.add(s)
            }
        }
        for (i in 0..word.length) {
            for (c in 'а' until 'я') {
                s = word.substring(0, i) + c.toString() + word.substring(i)
                if (s.isNotEmpty() && s[0] == firstChar)
                    result.add(s)
            }
        }
        return result
    }

    private fun canUse(s: String): Boolean {
        return s.length > 1 &&
                mDictionary.containsKey(s) &&
                mDictionary[s]!!.isNotUsed() &&
                mExclusions.hasNoExclusions(s)
    }

    fun reset() {
        mDictionary.values.forEach { it.resetUsageFlag() }
    }

    fun dispose() {
        mDictionary.clear()
        mSubDictionary.clear()
    }

    enum class CityResult {
        OK, CITY_NOT_FOUND, ALREADY_USED
    }

    internal class CityProperties(var diff: Byte, var countryCode: Short) {
        fun flipUsageFlag() {
            diff = (diff * -1).toByte()
        }

        fun resetUsageFlag() {
            if (diff < 0) diff = (-diff).toByte()
        }

        fun isNotUsed() = diff > 0
    }
}