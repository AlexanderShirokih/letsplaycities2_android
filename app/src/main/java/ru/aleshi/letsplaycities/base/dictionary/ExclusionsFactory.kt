package ru.aleshi.letsplaycities.base.dictionary

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import ru.aleshi.letsplaycities.FileProvider
import ru.aleshi.letsplaycities.Localization
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import javax.inject.Inject

/**
 * Factory interface for creating and loading [ExclusionsService] instance
 */
class ExclusionsFactory @Inject constructor(
    private val fileProvider: FileProvider,
    private val countryListLoaderService: CountryListLoaderService,
    @Localization("exclusion-errors")
    private val errMessages: Map<ExclusionsServiceImpl.ErrorCode, String>
) {

    /**
     * Return [Single] of [ExclusionsService] that loads its instance
     */
    fun load(): Single<ExclusionsService> =
        Single.fromCallable(::loadBlocking)
            .subscribeOn(Schedulers.io())

    private fun loadBlocking(): ExclusionsService {
        val exclusionsList = BufferedReader(InputStreamReader(fileProvider.open("others.txt")))
            .lineSequence()
            .mapNotNull { line -> line.takeIf { it.isNotEmpty() }?.split("|") }
            .map {
                it[0] to ExclusionsServiceImpl.Exclusion(
                    ExclusionsServiceImpl.ExclusionType.values()[it[1].toInt()],
                    it[2]
                )
            }
            .toMap()

        val states = BufferedReader(InputStreamReader(fileProvider.open("states.txt")))
            .readLines()

        val countries = runBlocking { countryListLoaderService.loadCountryList() }
            .map { it.copy(name = it.name.toLowerCase(Locale.getDefault())) }

        return ExclusionsServiceImpl(
            exclusionsList,
            errMessages,
            countries,
            states
        )
    }

}