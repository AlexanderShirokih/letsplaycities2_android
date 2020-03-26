package ru.aleshi.letsplaycities.base.dictionary

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Implementation of [DictionaryService]
 * @param dictionary [HashMap] with city name as key, and its properties as value
 */
class DictionaryServiceImpl constructor(
    private val dictionary: HashMap<String, CityProperties>
) : DictionaryService {

    /**
     * Internal difficulty reference
     */
    private var _difficulty: Byte = 1

    /**
     * Property to access [_difficulty]
     */
    override var difficulty: Byte
        get() = _difficulty
        set(value) {
            _difficulty = value.coerceIn(1, 3)
        }

    /**
     * @see DictionaryService.checkCity
     */
    override fun checkCity(city: String): Flowable<CityResult> {
        return Flowable.fromCallable {
                if (!dictionary.containsKey(city))
                    CityResult.CITY_NOT_FOUND
                else
                    if (dictionary[city]!!.diff < 0)
                        CityResult.ALREADY_USED
                    else
                        CityResult.OK
            }.subscribeOn(Schedulers.computation())
            .onBackpressureLatest()
    }

    override fun markUsed(city: String) {
        dictionary[city]?.markUsed()
    }

    override fun getCountryCode(city: String): Short = dictionary[city]?.countryCode ?: 0

    override fun getRandomWord(firstChar: Char): Maybe<String> {
        return Maybe.just(firstChar)
            .observeOn(Schedulers.computation())
            .flatMap {
                dictionary
                    .filterKeys { key -> key[0] == firstChar }
                    .filterValues { prop -> prop.diff > 0 && prop.countryCode != 0.toShort() && prop.diff <= difficulty }
                    .keys
                    .takeIf { it.isNotEmpty() }?.random()?.run { Maybe.just(this) }
                    ?: Maybe.empty()
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
                    it.onSuccess(candidates.distinct())

                for (s in list)
                    for (w in edits(s))
                        if (candidates.size < 4 && canUse(w) && !candidates.contains(w))
                            candidates.add(w)

                it.onSuccess(candidates.distinct())
            }
            .subscribeOn(Schedulers.computation())

    /**
     * Returns all cities in database.
     */
    override fun getAll(): Map<String, CityProperties> = dictionary

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

    /**
     * Checks whether city [s] is used before or not
     * @param s city for checking
     * @return `true` is given city is not used before
     */
    private fun canUse(s: String): Boolean {
        return s.length > 1 && dictionary[s]?.isNotUsed() ?: false
    }

    override fun reset() {
        dictionary.values.forEach { it.resetUsageFlag() }
    }

    override fun clear() {
        dictionary.clear()
    }

}