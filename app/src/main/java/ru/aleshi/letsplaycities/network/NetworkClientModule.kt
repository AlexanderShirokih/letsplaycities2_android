package ru.aleshi.letsplaycities.network

import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.BuildConfig
import ru.quandastudio.lpsclient.core.NetworkClient

@Module
class NetworkClientModule {

    @Provides
    fun provideNetworkClient(): NetworkClient =
        NetworkClient(false, NetworkClient.ConnectionType.WebSocket, BuildConfig.HOST)

}