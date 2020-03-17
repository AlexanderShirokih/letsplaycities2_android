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
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.ui.ActivityScope
import ru.aleshi.letsplaycities.ui.game.GameEntityWrapper
import ru.quandastudio.lpsclient.core.LPSMessage
import javax.inject.Inject

@ActivityScope
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

    val helpBtnVisible: ObservableBoolean = ObservableBoolean()
    val msgBtnVisible: ObservableBoolean = ObservableBoolean()

    private val _currentCities = MutableLiveData<List<GameEntityWrapper>>(mutableListOf())

    private val _currentState = MutableLiveData<GameState>(GameState.Initial)

    private val _wordState = MutableLiveData<WordCheckingResult>()

    private val _friendRequest = MutableLiveData<LPSMessage.LPSFriendRequest>()

    val cities: LiveData<List<GameEntityWrapper>> = _currentCities

    val state: LiveData<GameState> = _currentState

    val wordState: LiveData<WordCheckingResult> = _wordState

    val friendRequest: LiveData<LPSMessage.LPSFriendRequest> = _friendRequest

    /**
     * Call from UI to start game
     */
    fun startGame(gameSession: GameSession) {
        if (_currentState.value != GameState.Initial)
            throw GameException("Couldn't start game from state ${_currentState.value}")

        helpBtnVisible.set(gameSession.gameMode == GameMode.MODE_PVA)
        msgBtnVisible.set(gameSession.isMessagesAllowed())

        gameSession.users.forEach {
            it.score = 0
            when (it.position) {
                Position.LEFT -> {
                    imageLeft.set(it.imageRequest)
                    infoLeft.set(it.info)
                }
                Position.RIGHT -> {
                    imageRight.set(it.imageRequest)
                    infoRight.set(it.info)
                }
                else -> Unit
            }
        }

        presenter.start(this, gameSession)
    }

    /**
     * Call to process user input. In this case city correction won't be used.
     */
    fun processCityInputWithoutCorrection(input: String) {
        disposable += presenter.onUserInput(input, true)
            .subscribe(_wordState::postValue, ::showError)
    }

    /**
     * Call to process user input.
     */
    fun processCityInput(input: String) {
        disposable += presenter.onUserInput(input, false)
            .subscribe(_wordState::postValue, ::showError)
    }

    fun processMessage(message: String) {
        disposable += presenter.onMessage(message)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, ::showError)
    }

    /**
     * Called when player wants to surrender.
     */
    fun onPlayerSurrender() = presenter.onSurrender()

    /**
     * Called to update current game state.
     * @param state new [GameState] to bind.
     */
    override fun updateState(state: GameState) {
        _currentState.postValue(state)
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
        currentPosition.set(next.position)
        prev?.apply { updateInfo(this) }
    }

    /**
     * Called to put game entity (city or message) to the screen.
     */
    override fun putGameEntity(entity: GameEntity) {
        val list = _currentCities.value as MutableList
        val index = list.indexOfFirst { wrapper -> wrapper.gameEntity.areTheSameWith(entity) }

        if (index != -1) {
            //Item already exists, update content
            list[index] = GameEntityWrapper(entity)
        } else {
            //New item
            list.add(GameEntityWrapper(entity))
        }
        _currentCities.postValue(list)
    }

    /**
     * Called when user leaves from the game fragment
     */
    override fun dispose() {
        disposable.dispose()
        presenter.dispose()
    }

    override fun onCleared() {
        super.onCleared()
        dispose()
    }

    /**
     * Call to show error in UI
     */
    private fun showError(t: Throwable) {
        t.printStackTrace()
        _currentState.postValue(GameState.Error(t))
    }

    /**
     * Sends friend request to [userId] over game server.
     * @param userId id of user that we want to add to friends
     * @param onCompleted callback called in main thread when request was successfully sent
     */
    fun sendFriendRequest(userId: Int, onCompleted: () -> Unit) {
        presenter.sendFriendRequest(userId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onCompleted, ::showError)
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
            .subscribe(onCompleted, ::showError)
            .addTo(disposable)
    }

    private fun updateInfo(user: User) {
        when (user.position) {
            Position.LEFT ->
                infoLeft.set(user.info)
            Position.RIGHT ->
                infoRight.set(user.info)
            else -> Unit
        }
    }

    /**
     * Starts searching hint for player.
     */
    fun useHintForPlayer() {
        presenter.onPlayerHint()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, ::showError)
            .addTo(disposable)
    }

    /**
     * Calls [showMenuCallback] if user that currently bind at [position] has menu.
     */
    fun showMenu(position: Position, showMenuCallback: (u: User) -> Unit) {
        presenter.getCurrentSession().apply {
            val user = users.first { it.position == position }
            if (gameMode == GameMode.MODE_NET && user !is Player)
                showMenuCallback(user)
        }
    }

    override fun onFriendRequestResult(type: LPSMessage.LPSFriendRequest) =
        _friendRequest.postValue(type)
}