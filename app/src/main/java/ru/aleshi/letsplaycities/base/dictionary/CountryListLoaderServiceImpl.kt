package ru.aleshi.letsplaycities.base.dictionary

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.aleshi.letsplaycities.FileProvider
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Implementation of [CountryListLoaderService] that loads country list from
 * file as '|'-separated values.
 */
class CountryListLoaderServiceImpl @Inject constructor(
    private val fileProvider: FileProvider
) : CountryListLoaderService {

    /**
     * Loads country list from storage.
     */
    override suspend fun loadCountryList(): List<CountryEntity> {
        return withContext(Dispatchers.IO) {
            BufferedReader(InputStreamReader(fileProvider.open("countries.txt")))
                .readLines()
                .filter { it.isNotBlank() }
                .map { line ->
                    val (name, countryCode) = line.split("|")
                    CountryEntity(
                        name = name,
                        countryCode = countryCode.toShort()
                    )
                }
        }
    }
}