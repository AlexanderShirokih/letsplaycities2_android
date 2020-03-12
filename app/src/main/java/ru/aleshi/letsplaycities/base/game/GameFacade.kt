package ru.aleshi.letsplaycities.base.game

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.dictionary.CityResult
import ru.aleshi.letsplaycities.base.dictionary.DictionaryService
import ru.aleshi.letsplaycities.base.dictionary.ExclusionsService

/**
 * Facade wrapping access to game functions and properties such as access to database.
 */
open class GameFacade(
    private val dictionary: DictionaryService,
    private val exclusionsService: ExclusionsService,
    private val prefs: GamePreferences
) {

    /**
     * Returns random word from database starting at [firstChar] or [Maybe.empty] if no words left
     * starting at this [firstChar].
     */
    open fun getRandomWord(firstChar: Char): Maybe<String> = dictionary.getRandomWord(firstChar)

    /**
     * Checks [city] for any exclusions.
     * @return Description of exclusion for [city] or empty string if [city] has no exclusions.
     */
    open fun checkForExclusion(city: String) = exclusionsService.checkForExclusion(city)

    /**
     * Checks city in dictionary database.
     * @param city to be checked
     * @return [CityResult.OK] if city not used before, [CityResult.ALREADY_USED] if
     * [city] has already been used, [CityResult.CITY_NOT_FOUND] if [city] not found in dictionary.
     */
    open fun checkCity(city: String): Flowable<CityResult> = dictionary.checkCity(city)

    /**
     * Returns correction variants for [city] or empty list if there are no corrections available
     * or corrections is disabled in preferences.
     * @return correction variants for [city].
     */
    open fun getCorrections(city: String) =
        if (prefs.isCorrectionEnabled()) dictionary.getCorrectionVariants(city)
        else
            Single.just(emptyList())

    /**
     * Returns country code for city.
     * @param city input city
     * @return country code for [city] or `0` code for the [city] wasn't found.
     */
    open fun getCountryCode(city: String): Short = dictionary.getCountryCode(city)

    open val difficulty: Int = dictionary.difficulty.toInt()
}