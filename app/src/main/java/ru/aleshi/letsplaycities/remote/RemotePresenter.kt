package ru.aleshi.letsplaycities.remote

import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.RemoteUser
import ru.quandastudio.lpsclient.model.PlayerData
import javax.inject.Inject

class RemotePresenter @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val gameSessionBuilder: GameSession.GameSessionBuilder,
    private val remoteServer: RemoteServer,
    private val picasso: Picasso
) : RemoteContract.Presenter {

    enum class Status {
        WAITING_FOR_OPPONENTS
    }

    private val disposable = CompositeDisposable()


    lateinit var view: RemoteContract.View

    override fun onApplyView(view: RemoteContract.View) {
        this.view = view
    }

    override fun onStart() {
        disposable.add(remoteRepository
            .connect()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view.setStatus(Status.WAITING_FOR_OPPONENTS) }
            .subscribe({ pd ->
                play(remoteRepository.getPlayerData(), pd)
            },
                { err ->
                    view.onError(err.message!!)
                    onStop()
                },
                {
                    onStop()
                })
        )
    }

    private fun play(playerData: PlayerData, oppData: PlayerData) {
        val users = arrayOf(
            Player(playerData, picasso),
            RemoteUser(oppData, picasso)
        )

        view.onStartGame(
            gameSessionBuilder
                .server(remoteServer)
                .users(users)
                .build()
        )
    }

    override fun onStop() {
        remoteRepository.disconnect()
    }
}