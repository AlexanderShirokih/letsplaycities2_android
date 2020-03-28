package ru.aleshi.letsplaycities.base.dictionary

/**
 * Data class describing country.
 * @param name country name
 * @param countryCode flag code for country
 * @param hasSiblingCity `true` if there is a city with the same name as country
 */
data class CountryEntity(
    val name: String,
    val countryCode: Short,
    val hasSiblingCity: Boolean
)