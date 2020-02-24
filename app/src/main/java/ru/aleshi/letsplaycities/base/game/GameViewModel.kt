package ru.aleshi.letsplaycities.base.game

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.picasso.RequestCreator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.plusAssign
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.ui.game.GameEntityWrapper
import javax.inject.Inject

class GameViewModel @Inject constructor(
    private val presenter: GameContract.Presenter
) : ViewModel(), GameContract.ViewModel {

    private val disposable = CompositeDisposable()

    val imageLeft: ObservableField<RequestCreator> = ObservableField()
    val imageRight: ObservableField<RequestCreator> = ObservableField()

    val infoLeft: ObservableField<String> = ObservableField()
    val infoRight: ObservableField<String> = ObservableField()

    val currentPosition: ObservableField<Position> = ObservableField()

    var timer: ObservableField<String> = ObservableField()

    // TODO: Set this variables by injection
    val helpBtnVisible: ObservableBoolean = ObservableBoolean()
    val msgBtnVisible: ObservableBoolean = ObservableBoolean()

    private val _currentCities = MutableLiveData<List<GameEntityWrapper>>()

    private val _currentState = MutableLiveData<GameState>()

    private val _wordState = MutableLiveData<WordCheckingResult>()

    val cities: LiveData<List<GameEntityWrapper>> = _currentCities

    val state: LiveData<GameState> = _currentState

    val wordState: LiveData<WordCheckingResult> = _wordState

    /**
     * Call from UI to start game
     */
    fun startGame(gameSession: GameSession) {
        presenter.start(gameSession)
    }

    /*
    context.getString(
                        R.string.already_used,
                        StringUtils.toTitleCase(data.first)
                    )
     when (data.second) {
            Dictionary.CityResult.CITY_NOT_FOUND -> gameSession.correct(
                data.first,
                gameSession.view.context().getString(
                    R.string.city_not_found,
                    StringUtils.toTitleCase(data.first)
                )
            )
            Dictionary.CityResult.OK -> {
                sendCity(data.first)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        onSuccess()
                    }.subscribe(
                        {
                        },
                        { err ->
                            gameSession.notify(
                                gameSession.view.context().getString(
                                    R.string.unk_error,
                                    err.message
                                )
                            )
                        })
            }
        }*/

    /**
     * Call to process user input
     */
    fun processCityInput(input: String) {
        disposable += presenter.onUserInput(input).subscribe(_wordState::postValue, ::showError)
    }

    fun processMessage(message: String) {
        // TODO()
    }

    /**
     * Called to update current game state.
     * @param state new [GameState] to bind.
     */
    override fun updateState(state: GameState) {
        _currentState.postValue(state)
    }


    /**
     * Call to reset game state
     */
    fun restart() {
        //Try this...
        updateState(GameState.Started)
    }

    /**
     * Called on every game timer tick to update UI time.
     * @param time current time label
     */
    override fun updateTime(time: String) {
        timer.set(time)
    }

    /**
     * Called to switch active user highlight and update other user info.
     * @param prev user that has finished move or `null` if there are no moves before.
     * @param next user whose turn has come
     */
    override fun switchUser(prev: User?, next: User) {
        //Update score for previous user
        currentPosition.set(next.position)
        //TODO
    }

    /**
     * Called to put game entity (city or message) to the screen.
     */
    override fun putGameEntity(entity: GameEntity) {

    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
        presenter.dispose()
    }

    /**
     * Call to show error in UI
     */
    private fun showError(t: Throwable) {
        _currentState.postValue(GameState.Error(t))
    }

    private fun showMessage(msg: String) {
        TODO()
    }


    /**
     * Sends friend request to [userId] over game server.
     * @param userId id of user that we want to add to friends
     * @param onCompleted callback called in main thread when request was successfully sent
     */
    fun sendFriendRequest(userId: Int, onCompleted: () -> Unit) {
        presenter.sendFriendRequest(userId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(onCompleted)
            .doOnError { err -> showError(err) }
            .subscribe()
            .addTo(disposable)
    }

    /**
     * Sends ban message to [userId] over game server.
     * @param userId id of user that we want to ban
     * @param onCompleted callback called in main thread when request was successfully sent
     */
    fun banUser(userId: Int, onCompleted: () -> Unit) {
        presenter.banUser(userId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(onCompleted)
            .doOnError { err -> showError(err) }
            .subscribe()
            .addTo(disposable)
    }

}