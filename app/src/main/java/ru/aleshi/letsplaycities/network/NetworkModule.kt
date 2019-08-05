package ru.aleshi.letsplaycities.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import ru.aleshi.letsplaycities.BuildConfig
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.core.NetworkClient
import javax.inject.Singleton

@Module
abstract class NetworkModule {

    @Binds
    abstract fun networkPresenter(presenter: NetworkPresenterImpl): NetworkContract.Presenter

    @Module
    companion object {
        @JvmStatic
        @Singleton
        @Provides
        fun networkRepository(client: NetworkClient, token: Single<String>): NetworkRepository {
            return NetworkRepository(client, token)
        }

        @JvmStatic
        @Provides
        fun networkClient(): NetworkClient {
            return NetworkClient(BuildConfig.HOST)
        }

        @JvmStatic
        @Provides
        fun token(): Single<String> {
            return NetworkUtils.getToken()
        }
    }
}