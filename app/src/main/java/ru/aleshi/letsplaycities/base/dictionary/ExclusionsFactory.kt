package ru.aleshi.letsplaycities.base.dictionary

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.FileProvider
import ru.aleshi.letsplaycities.Localization
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Factory interface for creating and loading [ExclusionsService] instance
 */
class ExclusionsFactory @Inject constructor(
    private val fileProvider: FileProvider,
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

        val countries = BufferedReader(InputStreamReader(fileProvider.open("countries.txt")))
            .readLines()
        val states = BufferedReader(InputStreamReader(fileProvider.open("states.txt")))
            .readLines()

        return ExclusionsServiceImpl(
            exclusionsList,
            errMessages,
            countries,
            states
        )
    }

}