package ru.aleshi.letsplaycities.ui.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.aleshi.letsplaycities.ui.FetchState
import ru.quandastudio.lpsclient.core.LpsApi
import ru.quandastudio.lpsclient.model.RequestType
import javax.inject.Inject

class FriendRequestViewModel @Inject constructor(private val api: LpsApi) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val mState: MutableLiveData<FetchState> = MutableLiveData(
        FetchState.FinishState)

    val state: LiveData<FetchState>
        get() = mState

    fun onDecline(userId: Int) {
        disposable.add(
            api.sendGameRequestResult(userId, RequestType.DENY)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { mState.postValue(FetchState.LoadingState) }
                .subscribe({ mState.postValue(FetchState.FinishState) },
                    { error -> FetchState.ErrorState(error) })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}