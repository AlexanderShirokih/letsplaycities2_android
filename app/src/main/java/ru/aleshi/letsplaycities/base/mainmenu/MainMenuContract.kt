package ru.aleshi.letsplaycities.base.mainmenu

import ru.aleshi.letsplaycities.base.game.GameSession

interface MainMenuContract {

    interface MainMenuView {
        fun startGame(gameSession: GameSession)
    }

    interface MainMenuPresenter {
        fun startGame(hasLocalOpponents: Boolean)
    }
}