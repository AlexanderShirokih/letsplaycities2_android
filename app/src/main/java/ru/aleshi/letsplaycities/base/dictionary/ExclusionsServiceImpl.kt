package ru.aleshi.letsplaycities.base.dictionary

import ru.aleshi.letsplaycities.base.dictionary.ExclusionsServiceImpl.ErrorCode
import ru.aleshi.letsplaycities.utils.StringUtils.toTitleCase
import java.util.*

/**
 * Manages exclusions for cities.
 * Exclusion types:
 *  - City was renamed,
 *  - Incomplete name (for ex. city name of two words, but one given
 *  - Alternative name (city has two equal names, but only one name can be used in one game)
 *  - Not a city (the city not have city status (ex. village)
 *  - Region name ( historical regions, states, etc.)
 *  - Country name (it's not a city, but country name)
 *
 *  @param exclusionsList map of the city name and its exclusion type
 *  @param errMessages map of string representations of the [ErrorCode]
 *  @param countries list of country names
 *  @param states list of USA state names
 */
class ExclusionsServiceImpl(
    private val exclusionsList: Map<String, Exclusion>,
    private val errMessages: Map<ErrorCode, String>,
    private val countries: List<CountryEntity>,
    private val states: List<String>
) : ExclusionsService {

    /**
     * Error code to be used as key for its string representation
     */
    enum class ErrorCode {
        THIS_IS_A_COUNTRY,
        THIS_IS_A_STATE,
        RENAMED_CITY,
        INCOMPLETE_CITY,
        NOT_A_CITY,
        THIS_IS_NOT_A_CITY
    }

    /**
     * Exclusion kind
     */
    enum class ExclusionType {
        CITY_WAS_RENAMED,
        ALTERNATIVE_NAME,
        INCOMPLETE_NAME,
        NOT_A_CITY,
        REGION_NAME
    }

    /**
     * Structure containing exclusion type and its description|alternative
     * @param type the type of the exclusion
     * @param thing description of exclusion or its alternative writing
     */
    class Exclusion(var type: ExclusionType, var thing: String)

    override fun checkForExclusion(city: String): String {
        if (countries.any { it.name == city })
            return String.format(
                errMessages.getValue(ErrorCode.THIS_IS_A_COUNTRY),
                city.toTitleCase()
            )

        if (states.contains(city))
            return String.format(
                errMessages.getValue(ErrorCode.THIS_IS_A_STATE),
                city.toTitleCase()
            )

        return checkCity(city)
    }

    private fun checkCity(city: String): String {
        val ex = exclusionsList[city] ?: return ""
        return when (ex.type) {
            ExclusionType.CITY_WAS_RENAMED ->  // Город был переименован
                String.format(
                    errMessages.getValue(ErrorCode.RENAMED_CITY),
                    city.toTitleCase(),
                    ex.thing.toTitleCase()
                )
            ExclusionType.INCOMPLETE_NAME ->  // Неполное название
                String.format(
                    errMessages.getValue(ErrorCode.INCOMPLETE_CITY),
                    ex.thing.toTitleCase()
                )
            ExclusionType.NOT_A_CITY ->  // Географические объекты - не города
                String.format(
                    errMessages.getValue(ErrorCode.NOT_A_CITY),
                    city.toTitleCase(),
                    ex.thing
                )
            ExclusionType.REGION_NAME ->  // Исторические название областей
                String.format(
                    errMessages.getValue(ErrorCode.THIS_IS_NOT_A_CITY),
                    city.toTitleCase()
                )
            else -> ""

        }
    }

    override fun getAlternativeName(city: String): String? {
        return exclusionsList[city]?.takeIf { it.type == ExclusionType.ALTERNATIVE_NAME }?.thing
    }

    override fun hasNoExclusions(input: String): Boolean {
        val word = input.trim().toLowerCase(Locale.getDefault())
        return countries.none { it.name == word } && !states.contains(word) && !exclusionsList.contains(
            word
        )
    }

}