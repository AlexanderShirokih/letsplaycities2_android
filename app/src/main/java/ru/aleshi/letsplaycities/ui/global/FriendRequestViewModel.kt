package ru.aleshi.letsplaycities.ui.global

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.ui.FetchState
import ru.quandastudio.lpsclient.core.LpsRepository
import javax.inject.Inject

class FriendRequestViewModel @Inject constructor(private val lpsRepository: LpsRepository) :
    ViewModel() {

    private val disposable = CompositeDisposable()
    private val mState: MutableLiveData<FetchState> = MutableLiveData()

    val state: LiveData<FetchState>
        get() = mState

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun sendResult(receiverId: Int, isAccepted: Boolean) {
        disposable.add(
            lpsRepository.sendFriendRequestResult(receiverId, isAccepted)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { mState.postValue(FetchState.LoadingState) }
                .subscribe({ mState.postValue(FetchState.FinishState) },
                    { error -> mState.postValue(FetchState.ErrorState(error)) })
        )
    }
}