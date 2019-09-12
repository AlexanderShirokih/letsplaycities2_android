package ru.aleshi.letsplaycities.remote

import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.player.GamePlayerDataFactory
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.aleshi.letsplaycities.ui.ActivityScope
import ru.quandastudio.lpsclient.model.PlayerData

@Module
abstract class RemoteModule {

    @Binds
    abstract fun remotePresenter(presenter: RemotePresenter): RemoteContract.Presenter

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesPlayerData(
            gamePlayerDataFactory: GamePlayerDataFactory,
            gamePreferences: GamePreferences
        ): PlayerData {
            return gamePlayerDataFactory.load(gamePreferences)!!
        }

        @JvmStatic
        @ActivityScope
        @Provides
        fun remoteRepository(server: LPSServer): RemoteRepository {
            return RemoteRepository(server)
        }
    }
}