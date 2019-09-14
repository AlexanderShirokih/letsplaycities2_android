package ru.aleshi.letsplaycities.remote

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.base.player.GamePlayerDataFactory
import ru.aleshi.letsplaycities.network.NetworkUtils
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
        ): Single<PlayerData> {
            return Single.create<PlayerData> {
                val versionInfo = BuildConfig.VERSION_NAME to BuildConfig.VERSION_CODE
                NetworkUtils.createPlayerData(
                    versionInfo,
                    { playerData: PlayerData -> it.onSuccess(playerData) }, gamePreferences,
                    gamePlayerDataFactory,
                    authDataFactory.loadFromPreferences(gamePreferences)
                )
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