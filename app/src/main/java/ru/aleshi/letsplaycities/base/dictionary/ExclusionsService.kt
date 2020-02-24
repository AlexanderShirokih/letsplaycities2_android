package ru.aleshi.letsplaycities.base.dictionary

/**
 * Describes interface for city exclusions.
 */
interface ExclusionsService {

    /**
     * Checks [city] for any exclusions except of alternative names.
     * @return Description of exclusion for [city] or empty string if [city] has no exclusions.
     */
    fun checkForExclusion(city: String): String

    /**
     * Checks for any kind of exclusions.
     * @return true` if [input] has no exclusions, `false` otherwise.
     */
    fun hasNoExclusions(input: String): Boolean

    /**
     * Returns alternative name for given [city] or `null` if it found.
     * @param city city for checking its alternative name
     * @return alternative name of this city or `null` there are no names found.
     */
    fun getAlternativeName(city: String): String?

}