package ru.aleshi.letsplaycities.base.dictionary

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.base.NoWordsLeftException

class DictionaryServiceImpl constructor(
    private val dictionary: HashMap<String, CityProperties>,
    private val subDictionary: HashMap<Char, ArrayList<String>>
) : DictionaryService {

    private var _difficulty: Byte = 1

    override var difficulty: Byte
        get() = _difficulty
        set(value) {
            _difficulty = value.coerceIn(1, 4)
        }

    override fun checkCity(city: String): Single<CityResult> {
        return Single.fromCallable {
            if (!dictionary.containsKey(city)) {
                CityResult.CITY_NOT_FOUND
            } else {
                val prop = dictionary[city]!!
                if (prop.diff < 0)
                    CityResult.ALREADY_USED
                else {
                    CityResult.OK
                }
            }
        }.subscribeOn(Schedulers.computation())
    }

    override fun getCountryCode(city: String): Short {
        dictionary[city]?.run {
            return countryCode
        }
        return 0
    }


    //TODO: Refactor
    override fun getRandomWord(firstChar: Char): Maybe<String> {
        return Maybe.just(firstChar)
            .observeOn(Schedulers.computation())
            .filter { subDictionary.containsKey(it) }
            .flatMap {
                val ready = ArrayList<String>()
                val list = subDictionary[it]!!
                for (s in list) {
                    val cp = dictionary[s]!!
                    val b = cp.diff
                    if (b > 0 && cp.countryCode != 0.toShort() && b == difficulty)
                        ready.add(s)
                }
                if (ready.isEmpty()) {
                    Maybe.error<String>(NoWordsLeftException)
                }
                val word = ready[(0 until ready.size).random()]
                ready.clear()
                Maybe.just(word)
            }
    }

    override fun getCorrectionVariants(city: String): Single<List<String>> =
        Single.create<List<String>> {
            val list = edits(city)
            val candidates = ArrayList<String>()
            for (s in list) {
                // Max 3 words
                if (candidates.size == 3)
                    break
                if (canUse(s))
                    candidates.add(s)
            }

            if (candidates.isNotEmpty())
                it.onSuccess(candidates)

            for (s in list)
                for (w in edits(s))
                    if (candidates.size < 4 && canUse(w) && !candidates.contains(w))
                        candidates.add(w)

            it.onSuccess(candidates)
        }


    private fun edits(word: String): ArrayList<String> {
        val result = ArrayList<String>()
        val firstChar = word[0]
        var s: String

        for (i in word.indices) {
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
        for (i in word.indices) {
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
        return s.length > 1 && dictionary[s]?.isNotUsed() ?: false
    }

    override fun reset() {
        dictionary.values.forEach { it.resetUsageFlag() }
    }

    override fun clear() {
        dictionary.clear()
        subDictionary.clear()
    }


    class CityProperties(var diff: Byte, var countryCode: Short) {
        fun flipUsageFlag() {
            diff = (diff * -1).toByte()
        }

        fun resetUsageFlag() {
            if (diff < 0) diff = (-diff).toByte()
        }

        fun isNotUsed() = diff > 0
    }
}