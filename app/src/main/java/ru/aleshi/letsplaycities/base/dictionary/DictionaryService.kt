package ru.aleshi.letsplaycities.base.dictionary

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Interface that provides functions for access to dictionary database.
 */
interface DictionaryService {

    /**
     * Current dictionary difficulty. Used for getting random city.
     * Value from 1 to 3, where 1 is easiest and 3 hardest
     * @return current difficulty
     * @see Difficulty
     */
    var difficulty: Byte

    /**
     * Checks city in dictionary database.
     * @param cityProvider returns city to be checked
     * @return [Observable] with [CityResult.OK] if city not used before, [CityResult.ALREADY_USED] if
     * city has already been used, [CityResult.CITY_NOT_FOUND] if city not found in dictionary.
     */
    fun checkCity(cityProvider: () -> String): Flowable<CityResult>

    /**
     * Returns random word from database starting at [firstChar].
     * @return [Maybe] of random city or [Maybe.empty] if there are no available words
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
     * Returns all cities in database.
     */
    fun getAll(): Map<String, CityProperties>

    /**
     * Marks [city] as already used
     */
    fun markUsed(city: String)

    /**
     * Used to clean up all resources used by dictionary.
     */
    fun clear()

    /**
     * Used to reset usage flags and other data to it's default state
     */
    fun reset()

    /**
     * Game difficulty values
     */
    companion object Difficulty {
        const val EASY = 1
        const val MEDIUM = 2
        const val HARD = 3
    }

}