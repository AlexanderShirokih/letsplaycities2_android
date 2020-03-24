package ru.aleshi.letsplaycities.ui.citieslist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for [CitiesListFragment].
 */
class CitiesListViewModel : ViewModel() {

    /**
     * Contains list of countries that do <b>not</b> pass the filter.
     */
    val citiesFilter = MutableLiveData<List<Short>>()

}