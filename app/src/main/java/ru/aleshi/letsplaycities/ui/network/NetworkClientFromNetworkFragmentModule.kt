package ru.aleshi.letsplaycities.ui.network

import dagger.Module
import dagger.Provides
import ru.quandastudio.lpsclient.core.NetworkClient

@Module
class NetworkClientFromNetworkFragmentModule {

    @Provides
    fun provideNetworkClient(fragment: NetworkFragment): NetworkClient {
        val args = NetworkFragmentArgs.fromBundle(fragment.requireArguments())
        return if(args.port > 0) NetworkClient(args.isLocal, args.host, args.port) else NetworkClient(args.isLocal, args.host)
    }
}