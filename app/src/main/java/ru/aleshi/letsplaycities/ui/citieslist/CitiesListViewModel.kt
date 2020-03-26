package ru.aleshi.letsplaycities.ui.citieslist

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.rx2.await
import ru.aleshi.letsplaycities.base.dictionary.CityProperties
import ru.aleshi.letsplaycities.base.dictionary.DictionaryFactory
import ru.aleshi.letsplaycities.ui.FetchState
import ru.aleshi.letsplaycities.utils.StringUtils.toTitleCase
import java.util.*
import javax.inject.Inject


typealias DictionaryList = Map<String, CityProperties>

/**
 * ViewModel for [CitiesListFragment].
 */
class CitiesListViewModel @Inject constructor(
    private val dictionaryFactory: DictionaryFactory
) : ViewModel() {

    private val dictionary: Flow<DictionaryList> = flow {
        Log.d("TAG", "Begin loading dictionary")
        emit(dictionaryFactory.load().await().getAll())
        Log.d("TAG", "Stop loading dictionary")
    }
        .onStart { viewState.postValue(FetchState.LoadingState) }
        .onCompletion { viewState.postValue(FetchState.FinishState) }
        .flowOn(Dispatchers.IO)

    /**
     * Contains current city filter text.
     */
    val cityFilterChannel = ConflatedBroadcastChannel("")

    /**
     * Contains list of countries that do <b>not</b> pass the filter.
     */
    val countryListFilterChannel = ConflatedBroadcastChannel<List<Short>>(emptyList())

    /**
     * Emits value when state changes from loading to finished and backward.
     */
    val viewState = MutableLiveData<FetchState>()

    /**
     * Data class describing filter params.
     */
    data class CityListFilter(
        val cityNameFilter: String,
        val countryCodeFilter: List<Short>
    )

    private val filterFlow: Flow<CityListFilter> = cityFilterChannel
        .asFlow()
        .combine(countryListFilterChannel.asFlow()) { cityName: String, countryFilter: List<Short> ->
            Log.d("TAG", "Combining filters")
            CityListFilter(cityName, countryFilter)
        }

    val filteredCities = dictionary
        .combine(filterFlow) { dic: DictionaryList, filter: CityListFilter -> dic to filter }
        .onEach {
            Log.d("TAG", "Dictionary and filters combined together!")
        }
        .flatMapMerge { filterCities(it) }
        .flowOn(Dispatchers.Default)
        .conflate()
        .asLiveData()

    private fun filterCities(citiesWithFilter: Pair<DictionaryList, CityListFilter>) =
        flow {
            Log.d("TAG", "Start filtering")
            viewState.postValue(FetchState.LoadingState)
            val dictionary = citiesWithFilter.first
            val blockedCountries = citiesWithFilter.second.countryCodeFilter
            val cityFilter = citiesWithFilter.second.cityNameFilter.toLowerCase(Locale.getDefault())

            emit(dictionary
                .filter { entry ->
                    !blockedCountries.contains(entry.value.countryCode) && (cityFilter.isBlank() ||
                            entry.key.split(" ", "-").any { it.startsWith(cityFilter) })
                }
                .map { entry ->
                    CityItem(
                        city = entry.key.toTitleCase(),
                        country = "(TODO)",
                        countryCode = entry.value.countryCode
                    )
                }
                .sortedBy { it.city }
            )
            viewState.postValue(FetchState.FinishState)
            Log.d("TAG", "End filtering!")
        }

    override fun onCleared() {
        super.onCleared()
        cityFilterChannel.close()
        countryListFilterChannel.close()
    }
}