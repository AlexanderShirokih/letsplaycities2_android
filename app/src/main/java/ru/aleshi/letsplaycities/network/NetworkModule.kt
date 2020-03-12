package ru.aleshi.letsplaycities.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import ru.aleshi.letsplaycities.ui.ActivityScope
import ru.aleshi.letsplaycities.ui.network.FbToken
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.core.NetworkClient
import javax.inject.Singleton

@Module(includes = [NetworkModule.Submodule::class])
class NetworkModule {

    @ActivityScope
    @Provides
    fun networkRepository(client: NetworkClient, @FbToken token: Single<String>): NetworkRepository {
        return NetworkRepository(client, token)
    }

    @Module
    interface Submodule {
        @Binds
        abstract fun networkPresenter(presenter: NetworkPresenterImpl): NetworkContract.Presenter
    }
}