package ru.aleshi.letsplaycities.remote

import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.base.player.GamePlayerDataFactory
import ru.aleshi.letsplaycities.remote.internal.Connection
import ru.aleshi.letsplaycities.remote.internal.LPSServerImpl
import ru.aleshi.letsplaycities.remote.internal.SocketConnection
import ru.aleshi.letsplaycities.ui.ActivityScope
import ru.quandastudio.lpsclient.model.PlayerData

@Module
abstract class RemoteModule {

    @Binds
    abstract fun remotePresenter(presenter: RemotePresenter): RemoteContract.Presenter

    @Binds
    abstract fun connection(sockerConnection: SocketConnection): Connection

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesPlayerData(
            authDataFactory: GameAuthDataFactory,
            gamePlayerDataFactory: GamePlayerDataFactory,
            gamePreferences: GamePreferences
        ): PlayerData {
            val authData = authDataFactory.loadFromPreferences(gamePreferences)
            return gamePlayerDataFactory.create(authData).apply {
                clientVersion = BuildConfig.VERSION_NAME
                clientBuild = BuildConfig.VERSION_CODE
                canReceiveMessages = gamePreferences.canReceiveMessages()
            }
        }

        @JvmStatic
        @ActivityScope
        @Provides
        fun remoteRepository(server: LPSServerImpl): RemoteRepository {
            return RemoteRepository(server)
        }
    }
}