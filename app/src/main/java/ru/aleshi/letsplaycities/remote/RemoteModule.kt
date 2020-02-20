package ru.aleshi.letsplaycities.remote

import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.AppVersionInfo
import ru.aleshi.letsplaycities.GsonModule
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.remote.internal.*
import ru.aleshi.letsplaycities.ui.ActivityScope
import ru.quandastudio.lpsclient.core.JsonMessage
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.VersionInfo

@Module(includes = [GsonModule::class])
abstract class RemoteModule {

    @Binds
    abstract fun remotePresenter(presenter: RemotePresenter): RemoteContract.Presenter

    @Binds
    abstract fun connection(sockerConnection: SocketConnection): Connection

    @Binds
    abstract fun messagePipe(pipe: JsonMessagePipe): MessagePipe

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesPlayerData(
            authDataFactory: GameAuthDataFactory,
            gamePreferences: GamePreferences,
            @AppVersionInfo versionInfo: VersionInfo
        ): PlayerData = PlayerData(
            authData = authDataFactory.load(),
            versionInfo = versionInfo,
            canReceiveMessages = gamePreferences.canReceiveMessages(),
            pictureHash = gamePreferences.pictureHash
        )

        @JvmStatic
        @ActivityScope
        @Provides
        fun remoteRepository(server: LPSServerImpl): RemoteRepository {
            return RemoteRepository(server)
        }

        @JvmStatic
        @ActivityScope
        @Provides
        fun jsonMessage(): JsonMessage = JsonMessage()
    }
}