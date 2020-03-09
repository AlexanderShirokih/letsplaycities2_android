package ru.aleshi.letsplaycities.ui.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.ui.FetchState
import ru.quandastudio.lpsclient.core.LpsRepository
import javax.inject.Inject

class NetworkFetchViewModel @Inject constructor(private val apiRepo: LpsRepository) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    private val mState: MutableLiveData<FetchState> = MutableLiveData(
        FetchState.FinishState)

    val state: LiveData<FetchState>
        get() = mState

    fun withApi(action: (api: LpsRepository) -> Disposable) {
        disposable.add(action(apiRepo))
    }

    fun <T> fetchData(fetchFunction: (api: LpsRepository) -> Maybe<List<T>>) {
        mState.postValue(FetchState.LoadingState)
        disposable.add(
            fetchFunction(apiRepo)
                .subscribeOn(Schedulers.io())
                .subscribe({ data ->
                    mState.postValue(
                        FetchState.DataState(
                            data
                        )
                    )
                }, { error ->
                    error.printStackTrace()
                    mState.postValue(FetchState.ErrorState(error))
                }, {
                    mState.postValue(FetchState.FinishState)
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}