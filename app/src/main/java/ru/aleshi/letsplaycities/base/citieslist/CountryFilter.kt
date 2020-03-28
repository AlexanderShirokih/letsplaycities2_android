package ru.aleshi.letsplaycities.base.citieslist

import ru.aleshi.letsplaycities.base.dictionary.CountryEntity

/**
 * Wrapper for list of accepted countries.
 * @param isAllPresent if `true` filter accepts all countries, even if [acceptedCountries] is empty
 * @param acceptedCountries list of countries that filter should pass.
 */
data class CountryFilter(
    val acceptedCountries: List<CountryEntity>,
    val isAllPresent: Boolean = true
)