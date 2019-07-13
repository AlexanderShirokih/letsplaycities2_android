package ru.aleshi.letsplaycities.base

import android.content.Context
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.Utils
import java.io.BufferedReader
import java.io.InputStreamReader


class Exclusions private constructor(
    private val exclusionsList: HashMap<String, Exclusion>,
    private val countries: List<String>,
    private val states: List<String>,
    private val errMsgs: List<String>
) {

    class Exclusion(var type: Int, var thing: String)

    companion object Factory {
        fun load(context: Context): Single<Exclusions> {
            return Single.just(context)
                .subscribeOn(Schedulers.io())
                .map(::loadBlocking)
        }

        private fun loadBlocking(context: Context): Exclusions {
            val exclusionsList = HashMap<String, Exclusion>()

            BufferedReader(InputStreamReader(context.assets.open("others.txt")))
                .lineSequence()
                .filter { it.isNotEmpty() }
                .map { it.split("|") }
                .forEach { exclusionsList[it[0]] = Exclusion(it[1].toInt(), it[2]) }

            val countries = BufferedReader(InputStreamReader(context.assets.open("countries.txt")))
                .readLines()
            val states = BufferedReader(InputStreamReader(context.assets.open("states.txt")))
                .readLines()

            val errMsgs = listOf(
                context.getString(R.string.this_is_a_country),
                context.getString(R.string.this_is_a_state),
                context.getString(R.string.renamed_city),
                context.getString(R.string.uncompleted_city),
                context.getString(R.string.not_city),
                context.getString(R.string.this_is_not_a_city)
            )

            return Exclusions(exclusionsList, countries, states, errMsgs)
        }
    }

    fun check(city: String): Pair<String, String?> {
        val result = city.trim().toLowerCase()
        if (countries.contains(result))
            return city to String.format(errMsgs[0], Utils.firstToUpper(result))

        if (states.contains(result))
            return city to String.format(errMsgs[1], Utils.firstToUpper(result))

        return city to checkCity(result)
    }

    //Usage: context.load().putWord(ex.thing)
    fun isAlternativeName(city: String): String? {
        val ex = exclusionsList[city] ?: return null
        return if (ex.type == 1) ex.thing else null
    }

    fun hasNoExclusions(input: String): Boolean {
        val word = input.trim().toLowerCase()
        return !countries.contains(word) && !states.contains(word) && !exclusionsList.contains(word)
    }

    fun dispose() {
        exclusionsList.clear()
    }

    private fun checkCity(city: String): String? {
        val ex = exclusionsList[city] ?: return null
        var msg: String? = null

        when (ex.type) {
            0 -> { // Город был переименован
                msg = String.format(errMsgs[2], Utils.firstToUpper(city), Utils.firstToUpper(ex.thing))
            }
            2 -> { // Неполное название
                msg = String.format(errMsgs[3], Utils.firstToUpper(ex.thing))
            }
            3 -> { // Географические объекты - не города
                msg = String.format(errMsgs[4], Utils.firstToUpper(city), ex.thing)
            }
            4 -> { // Исторические название областей
                msg = String.format(errMsgs[5], Utils.firstToUpper(city))
            }
        }
        return msg
    }
}