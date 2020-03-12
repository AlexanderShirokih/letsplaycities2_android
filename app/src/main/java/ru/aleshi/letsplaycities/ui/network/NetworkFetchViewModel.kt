package ru.aleshi.letsplaycities.ui.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.ui.FetchState
import ru.quandastudio.lpsclient.core.LpsRepository
import javax.inject.Inject

class NetworkFetchViewModel @Inject constructor(private val apiRepo: LpsRepository) : ViewModel() {

    private val mState: MutableLiveData<FetchState> = MutableLiveData()

    val state: LiveData<FetchState>
        get() = mState

    fun withApi(action: suspend (api: LpsRepository) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            action(apiRepo)
        }
    }

    fun <T> fetchData(fetchFunction: suspend (api: LpsRepository) -> List<T>) {
        viewModelScope.launch(Dispatchers.IO) {
            mState.postValue(FetchState.LoadingState)
            try {
                val data = fetchFunction(apiRepo)
                mState.postValue(FetchState.DataState(data))
            } catch (e: Exception) {
                mState.postValue(FetchState.ErrorState(e))
            }
        }
    }

}