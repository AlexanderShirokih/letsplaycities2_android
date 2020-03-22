package ru.aleshi.letsplaycities.base.dictionary

/**
 * Interface that provides functions for country list loading
 */
interface CountryListLoaderService {

    /**
     * Loads country list from storage.
     */
    suspend fun loadCountryList(): List<CountryEntity>

}