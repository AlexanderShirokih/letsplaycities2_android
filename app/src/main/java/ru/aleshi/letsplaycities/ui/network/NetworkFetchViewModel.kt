package ru.aleshi.letsplaycities.ui.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.ui.FetchState
import ru.quandastudio.lpsclient.core.LpsApi
import javax.inject.Inject

class NetworkFetchViewModel @Inject constructor(private val api: LpsApi) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    private val mState: MutableLiveData<FetchState> = MutableLiveData(
        FetchState.FinishState)

    val state: LiveData<FetchState>
        get() = mState

    fun withApi(action: (api: LpsApi) -> Disposable) {
        disposable.add(action(api))
    }

    fun <T> fetchData(fetchFunction: (api: LpsApi) -> Maybe<List<T>>) {
        mState.postValue(FetchState.LoadingState)
        disposable.add(
            fetchFunction(api)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ data ->
                    Log.d("TAG", "!!!! Success@ list=$data")
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