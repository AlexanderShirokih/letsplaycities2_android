package ru.aleshi.letsplaycities.ui.network

import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.network.AndroidBase64Provider
import ru.quandastudio.lpsclient.core.NetworkClient

@Module
class NetworkClientFromNetworkFragmentModule {

    @Provides
    fun provideNetworkClient(fragment: NetworkFragment): NetworkClient {
        val args = NetworkFragmentArgs.fromBundle(fragment.requireArguments())
        val connectionType =
            if (args.isLocal) NetworkClient.ConnectionType.PureSocket else NetworkClient.ConnectionType.WebSocket
        return if (args.port > 0) NetworkClient(
            AndroidBase64Provider,
            args.isLocal,
            connectionType,
            args.host,
            args.port
        ) else NetworkClient(AndroidBase64Provider, args.isLocal, connectionType, args.host)
    }
}