package ru.aleshi.letsplaycities.base.mainmenu

import ru.aleshi.letsplaycities.base.PlayerKind
import ru.aleshi.letsplaycities.base.game.GameMode
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.User
import javax.inject.Inject
import javax.inject.Provider

class MainMenuPresenter @Inject constructor(
    private val view: MainMenuContract.MainMenuView,
    private val server: BaseServer,
    @PlayerKind(PlayerKind.Type.LOCAL_PLAYER_1)
    private val local1: Provider<User>,
    @PlayerKind(PlayerKind.Type.LOCAL_PLAYER_2)
    private val local2: Provider<User>,
    @PlayerKind(PlayerKind.Type.ANDROID)
    private val android: Provider<User>,
    @PlayerKind(PlayerKind.Type.USER_PLAYER)
    private val player: Provider<User>
) : MainMenuContract.MainMenuPresenter {

    override fun startGame(hasLocalOpponents: Boolean) {
        val players: Array<User> = if (hasLocalOpponents)
            arrayOf(local1.get(), local2.get())
        else
            arrayOf(player.get(), android.get())

        view.startGame(
            GameSession(
                players,
                server,
                if (hasLocalOpponents) GameMode.MODE_PVP else GameMode.MODE_PVA
            )
        )
    }

}