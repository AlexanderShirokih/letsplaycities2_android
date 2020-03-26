package ru.aleshi.letsplaycities.ui.citieslist

/**
 * Model class representing city item with its country.
 */
data class CityItem(
    val city: String,
    val country: String,
    val countryCode: Short
)