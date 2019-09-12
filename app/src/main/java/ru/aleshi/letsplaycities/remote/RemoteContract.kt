package ru.aleshi.letsplaycities.remote

import ru.aleshi.letsplaycities.base.game.GameSession


interface RemoteContract {
    interface View {
        fun setStatus(status: RemotePresenter.Status)
        fun onError(message: String)
        fun onStartGame(gameSession: GameSession)
    }

    interface Presenter {
        fun onStart()
        fun onApplyView(view: View)
        fun onStop()
    }
}