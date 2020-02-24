package ru.aleshi.letsplaycities.base.mainmenu

import android.content.Context
import com.squareup.picasso.Picasso
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.AppVersionInfo
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.PlayerKind
import ru.aleshi.letsplaycities.base.player.Android
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.LocalServer
import ru.quandastudio.lpsclient.model.VersionInfo

@Module(includes = [MainMenuModule.SubModule::class])
class MainMenuModule {

    @Module
    interface SubModule {
        @Binds
        fun server(localServer: LocalServer): BaseServer

        @Binds
        fun presenter(presenter: MainMenuPresenter): MainMenuContract.MainMenuPresenter
    }

    @PlayerKind(PlayerKind.Type.ANDROID)
    @Provides
    fun androidPlayer(
        picasso: Picasso,
        context: Context, @AppVersionInfo versionInfo: VersionInfo
    ): User =
        Android(picasso, context.getString(R.string.android), versionInfo)

    @PlayerKind(PlayerKind.Type.USER_PLAYER)
    @Provides
    fun userPlayer(
        picasso: Picasso,
        context: Context, @AppVersionInfo versionInfo: VersionInfo
    ): User =
        Player(picasso, context.getString(R.string.player), versionInfo)

    @PlayerKind(PlayerKind.Type.LOCAL_PLAYER_1)
    @Provides
    fun localPlayer1(
        picasso: Picasso,
        context: Context, @AppVersionInfo versionInfo: VersionInfo
    ): User =
        Player(picasso, "${context.getString(R.string.player)} 1", versionInfo)

    @PlayerKind(PlayerKind.Type.LOCAL_PLAYER_2)
    @Provides
    fun localPlayer2(
        picasso: Picasso,
        context: Context, @AppVersionInfo versionInfo: VersionInfo
    ): User =
        Player(picasso, "${context.getString(R.string.player)} 2", versionInfo)
}