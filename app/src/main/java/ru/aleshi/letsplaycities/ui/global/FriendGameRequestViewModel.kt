package ru.aleshi.letsplaycities.ui.global

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.ui.FetchState
import ru.quandastudio.lpsclient.core.LpsRepository
import javax.inject.Inject

class FriendGameRequestViewModel @Inject constructor(private val lpsRepository: LpsRepository) :
    ViewModel() {

    private val disposable = CompositeDisposable()
    private val mState: MutableLiveData<FetchState> = MutableLiveData()

    val state: LiveData<FetchState>
        get() = mState

    fun onDecline(userId: Int) {
        disposable.add(
            lpsRepository.declineGameRequestResult(userId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { mState.postValue(FetchState.LoadingState) }
                .subscribe({ mState.postValue(FetchState.FinishState) },
                    { error -> mState.postValue(FetchState.ErrorState(error)) })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}