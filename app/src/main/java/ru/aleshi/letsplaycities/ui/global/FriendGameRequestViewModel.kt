package ru.aleshi.letsplaycities.ui.global

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.ui.FetchState
import ru.quandastudio.lpsclient.core.LpsRepository
import javax.inject.Inject

class FriendGameRequestViewModel @Inject constructor(private val lpsRepository: LpsRepository) :
    ViewModel() {

    private val mState: MutableLiveData<FetchState> = MutableLiveData()

    val state: LiveData<FetchState>
        get() = mState

    fun onDecline(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mState.postValue(FetchState.LoadingState)
            try {
                lpsRepository.declineGameRequestResult(userId)
            } catch (e: Exception) {
                mState.postValue(FetchState.ErrorState(e))
            }
            mState.postValue(FetchState.FinishState)
        }
    }

}