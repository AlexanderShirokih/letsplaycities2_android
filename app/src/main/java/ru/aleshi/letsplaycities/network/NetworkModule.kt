package ru.aleshi.letsplaycities.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.ui.ActivityScope
import ru.aleshi.letsplaycities.ui.network.FbToken
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.core.LpsApi
import ru.quandastudio.lpsclient.core.NetworkClient
import ru.quandastudio.lpsclient.model.Credentials

@Module
abstract class NetworkModule {

    @Binds
    abstract fun networkPresenter(presenter: NetworkPresenterImpl): NetworkContract.Presenter

    @Module
    companion object {
        @JvmStatic
        @ActivityScope
        @Provides
        fun networkRepository(client: NetworkClient, @FbToken token: Single<String>): NetworkRepository {
            return NetworkRepository(client, token)
        }

        @JvmStatic
        @Provides
        @FbToken
        fun token(): Single<String> {
            return NetworkUtils.getToken()
        }

        @JvmStatic
        @Provides
        fun provideCredentials(authDataFactory: GameAuthDataFactory): Credentials {
            return authDataFactory.load().credentials
        }

        @JvmStatic
        @Provides
        fun apiClient(credentials: Credentials): LpsApi {
            return LpsApi.create(Utils.getServerBaseUrl(), credentials)
        }
    }
}