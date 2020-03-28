package ru.aleshi.letsplaycities.base.citieslist

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.rx2.await
import ru.aleshi.letsplaycities.base.dictionary.CityProperties
import ru.aleshi.letsplaycities.base.dictionary.CountryEntity
import ru.aleshi.letsplaycities.base.dictionary.CountryListLoaderService
import ru.aleshi.letsplaycities.base.dictionary.DictionaryFactory
import ru.aleshi.letsplaycities.utils.StringUtils.toTitleCase
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.collections.ArrayList

typealias DictionaryList = Map<String, CityProperties>

/**
 * ViewModel for `CitiesListFragment`.
 */
@FlowPreview
@ExperimentalCoroutinesApi
class CitiesListViewModel @Inject constructor(
    private val dictionaryFactory: DictionaryFactory,
    private val countryListLoader: CountryListLoaderService
) : ViewModel() {

    /**
     * [LiveData] that once loads country list using [countryListLoader].
     */
    val countryList: LiveData<List<CountryEntity>> =
        liveData { emit(countryListLoader.loadCountryList()) }


    private val dictionary: Flow<DictionaryList> = flow {
        emit(dictionaryFactory.load().await().getAll())
    }
        .onStart { setLoading(true) }
        .onCompletion { setLoading(false) }
        .flowOn(Dispatchers.IO)

    /**
     * Contains current city filter text.
     */
    val cityFilterChannel = ConflatedBroadcastChannel("")

    /**
     * Contains list of countries that should pass the filter.
     */
    val countryListFilterChannel = ConflatedBroadcastChannel<CountryFilter>().apply {
        viewModelScope.launch {
            this@apply.offer(
                CountryFilter(
                    countryList.asFlow().first(),
                    true
                )
            )
        }
    }

    /**
     * Emit values when view model starts or ends loading something.
     */
    val loadingChannel = ConflatedBroadcastChannel<Boolean>()

    /**
     * Data class describing filter params.
     */
    data class CityListFilter(
        val cityNameFilter: String,
        val countryCodeFilter: CountryFilter
    )

    private val filterFlow: Flow<CityListFilter> = cityFilterChannel
        .asFlow()
        .debounce(1000L)
        .combine(countryListFilterChannel.asFlow()) { cityName: String, countryFilter: CountryFilter ->
            CityListFilter(
                cityName,
                countryFilter
            )
        }
        .distinctUntilChanged()

    val filteredCities = dictionary
        .combine(filterFlow) { dic: DictionaryList, filter: CityListFilter -> dic to filter }
        .flatMapMerge { filterCities(it) }
        .flowOn(Dispatchers.Default)
        .conflate()

    private val counter = AtomicInteger()

    /**
     * Increments or decrements loading [counter].
     * When [counter] value less or equal zero `false` will sent to [loadingChannel] otherwise `true` sent.
     */
    fun setLoading(isLoading: Boolean) {
        val count = if (isLoading)
            counter.incrementAndGet()
        else
            counter.decrementAndGet()

        loadingChannel.offer(count > 0)
    }

    /**
     * Sets [counter] to zero and offers `false` to channel.
     */
    fun forceStopLoading() {
        counter.set(0)
        loadingChannel.offer(false)
    }

    private fun filterCities(citiesWithFilter: Pair<DictionaryList, CityListFilter>) = flow {
        val dictionary = citiesWithFilter.first
        val countryList = citiesWithFilter.second.countryCodeFilter
        val cityFilter = citiesWithFilter.second.cityNameFilter.toLowerCase(Locale.getDefault())
        val deferredList = ArrayList<Deferred<Any>>(dictionary.size)

        coroutineScope {
            for (entry in dictionary) {
                deferredList += async {
                    // Fast pass if all countries checked or city name filter is empty
                    if ((countryList.isAllPresent || countryList.acceptedCountries.any { it.countryCode == entry.value.countryCode })
                        && (cityFilter.isBlank() || entry.key.split(" ", "-")
                            .any { it.startsWith(cityFilter) })
                    )
                        CityItem(
                            city = entry.key.toTitleCase(),
                            country = countryList.acceptedCountries.firstOrNull {
                                it.countryCode == entry.value.countryCode
                            }?.name?.let { "($it)" } ?: "",
                            countryCode = entry.value.countryCode
                        )
                    else
                        Unit
                }
            }
            val list = deferredList.awaitAll()
                .filterIsInstance<CityItem>()
                .sortedBy { it.city }
            emit(list to (list.size == dictionary.size))
        }
    }
        .onStart { setLoading(true) }
        .onCompletion { setLoading(false) }

    override fun onCleared() {
        super.onCleared()
        cityFilterChannel.close()
        countryListFilterChannel.close()
        loadingChannel.close()
    }
}