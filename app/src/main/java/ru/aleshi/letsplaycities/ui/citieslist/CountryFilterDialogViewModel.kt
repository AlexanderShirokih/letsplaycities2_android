package ru.aleshi.letsplaycities.ui.citieslist

import androidx.lifecycle.*
import ru.aleshi.letsplaycities.base.dictionary.CountryEntity
import ru.aleshi.letsplaycities.base.dictionary.CountryListLoaderService
import javax.inject.Inject

class CountryFilterDialogViewModel @Inject constructor(
    private val countryListLoader: CountryListLoaderService
) : ViewModel() {

    /**
     * [LiveData] that once loads country list using [countryListLoader].
     */
    private val countryList: LiveData<List<CountryEntity>> =
        liveData { emit(countryListLoader.loadCountryList()) }

    /**
     * [LiveData] that info about currently checked items in dialog's list.
     */
    private val checkboxes = MutableLiveData<MutableList<Boolean>>()

    /**
     * Combines data from [countryList] and [checkboxes].
     */
    val countryListWithCheckboxes: LiveData<List<Pair<CountryEntity, Boolean>>> =
        MediatorLiveData<List<Pair<CountryEntity, Boolean>>>().apply {
            addSource(countryList) {
                value = value
                    ?.zip(it) { old, new -> old.copy(first = new) }
                    ?: it.map { countryEntity -> countryEntity to true }
            }
            addSource(checkboxes) {
                value = value
                    ?.zip(it) { old, new -> old.copy(second = new) }
                    ?: it.map { isChecked -> CountryEntity("-", 0) to isChecked }
            }
        }

    /**
     * Emits `true` when after changes all items in checkbox list is `true`, `false` otherwise.
     */
    val checkedAll: LiveData<Boolean> = Transformations.map(checkboxes) { checkboxList ->
        checkboxList.all { isChecked -> isChecked }
    }

    /**
     * Handles event for 'Select All' button.
     * If all items selected, deselects all, in other cases all items will checked.
     */
    fun switchAll(allEnabled: Boolean) {
        val countryList = countryList.value
        if (countryList != null) {
            checkboxes.value = checkboxes.value
                ?.map { allEnabled }?.run { this as MutableList<Boolean> }
                ?: MutableList(countryList.size) { allEnabled }
        }
    }

    /**
     * Dispatches checkbox state to corresponding [LiveData].
     * @param position item index in list.
     * @param isChecked `true` if item become checked
     * @return `true` if updating value was successful (only if [countryList] has already filled,
     * `false` if [countryList] value is `null`.
     */
    fun setCheckboxState(position: Int, isChecked: Boolean): Boolean {
        val countryList = countryList.value
        if (countryList != null) {
            checkboxes.value = checkboxes.value?.apply {
                this[position] = isChecked
                checkboxes.value = this
            } ?: MutableList(countryList.size) { it != position || isChecked }
            return true
        }
        return false
    }

    /**
     * Sends list of currently *unselected* country codes to [liveData].
     * @param liveData data receiver
     */
    fun dispatchSelectedCitiesTo(liveData: MutableLiveData<List<Short>>) {
        countryListWithCheckboxes.value?.let { selectedCities ->
            liveData.postValue(
                selectedCities
                    .filterNot { it.second }
                    .map { it.first.countryCode }
            )
        }
    }
}