package ru.aleshi.letsplaycities.remote

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.GsonModule
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.remote.internal.*
import ru.aleshi.letsplaycities.ui.ActivityScope
import ru.quandastudio.lpsclient.core.JsonMessage
import ru.quandastudio.lpsclient.model.PlayerData

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
            gamePreferences: GamePreferences
        ): PlayerData = PlayerData(
            authData = authDataFactory.load(),
            clientVersion = BuildConfig.VERSION_NAME,
            clientBuild = BuildConfig.VERSION_CODE,
            canReceiveMessages = gamePreferences.canReceiveMessages()
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