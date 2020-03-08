package ru.aleshi.letsplaycities.base.mainmenu

import com.squareup.picasso.Picasso
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.AppVersionInfo
import ru.aleshi.letsplaycities.Localization
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.PlayerKind
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.base.player.Android
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.LocalServer
import ru.aleshi.letsplaycities.ui.ActivityScope
import ru.quandastudio.lpsclient.model.*

@Module(includes = [MainMenuModule.SubModule::class])
class MainMenuModule {

    @Module
    interface SubModule {
        @ActivityScope
        @Binds
        fun server(localServer: LocalServer): BaseServer

        @Binds
        fun presenter(presenter: MainMenuPresenter): MainMenuContract.MainMenuPresenter
    }

    @PlayerKind(PlayerKind.Type.ANDROID)
    @Provides
    fun androidPlayer(
        picasso: Picasso,
        @Localization("androidPlayerName")
        playerName: String, @AppVersionInfo versionInfo: VersionInfo
    ): User =
        Android(
            PlayerData(AuthData(playerName, AuthType.Native, Credentials(-1, "")), versionInfo),
            PictureSource(picasso, R.drawable.ic_android_big)
        )

    @PlayerKind(PlayerKind.Type.USER_PLAYER)
    @Provides
    fun userPlayer(
        server: BaseServer,
        picasso: Picasso,
        @Localization("playerName")
        playerName: String, @AppVersionInfo versionInfo: VersionInfo
    ): User =
        Player(
            server,
            PlayerData(AuthData(playerName, AuthType.Native, Credentials(-2, "")), versionInfo),
            picasso
        )

    @PlayerKind(PlayerKind.Type.LOCAL_PLAYER_1)
    @Provides
    fun localPlayer1(
        server: BaseServer,
        picasso: Picasso,
        @Localization("playerName")
        playerName: String, @AppVersionInfo versionInfo: VersionInfo
    ): User =
        Player(
            server,
            PlayerData(
                AuthData("$playerName 1", AuthType.Native, Credentials(-3, "")),
                versionInfo
            ),
            picasso
        )

    @PlayerKind(PlayerKind.Type.LOCAL_PLAYER_2)
    @Provides
    fun localPlayer2(
        server: BaseServer,
        picasso: Picasso,
        @Localization("playerName")
        playerName: String, @AppVersionInfo versionInfo: VersionInfo
    ): User =
        Player(
            server,
            PlayerData(
                AuthData("$playerName 2", AuthType.Native, Credentials(-4, "")),
                versionInfo
            ),
            picasso
        )
}