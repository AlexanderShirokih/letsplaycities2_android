package ru.aleshi.letsplaycities.base.dictionary

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Interface that provides functions for access to dictionary database.
 */
interface DictionaryService {

    /**
     * Current dictionary difficulty. Used for getting random city.
     * @return current difficulty
     */
    var difficulty: Byte

    /**
     * Checks city in dictionary database.
     * @param city to be checked
     * @return [Observable] with [CityResult.OK] if city not used before, [CityResult.ALREADY_USED] if
     * [city] has already been used, [CityResult.CITY_NOT_FOUND] if [city] not found in dictionary.
     */
    fun checkCity(city: String): Single<CityResult>

    /**
     * Returns random word from database starting at [firstChar].
     * @return [Maybe] of random city or [ru.aleshi.letsplaycities.base.NoWordsLeftException] if there are no available words
     * starting at [firstChar]
     */
    fun getRandomWord(firstChar: Char): Maybe<String>

    /**
     * Returns country code for [city] or `0` if country code for the [city] is not found.
     * @return country code for [city] or `0`
     */
    fun getCountryCode(city: String): Short

    /**
     * Returns correction variants for [city] or empty list if there are no corrections available.
     * @return correction variants for [city].
     */
    fun getCorrectionVariants(city: String): Single<List<String>>

    /**
     * Used to clean up all resources used by dictionary.
     */
    fun clear()

    /**
     * Used to reset usage flags and other data to it's default state
     */
    fun reset()

}