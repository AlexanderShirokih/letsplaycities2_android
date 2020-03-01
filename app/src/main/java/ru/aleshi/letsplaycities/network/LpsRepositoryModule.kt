package ru.aleshi.letsplaycities.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.core.CredentialsProvider
import ru.quandastudio.lpsclient.core.LpsApi
import ru.quandastudio.lpsclient.core.LpsRepository
import javax.inject.Singleton

@Module(includes = [LpsRepositoryModule.Submodule::class])
class LpsRepositoryModule {

    @Provides
    fun apiRepository(credentials: CredentialsProvider): LpsRepository {
        return LpsRepository(LpsApi.create(Utils.getServerBaseUrl(), credentials))
    }

    @Module
    interface Submodule {
        @Singleton
        @Binds
        abstract fun credentialsProvider(providerImpl: GameAuthDataFactory): CredentialsProvider
    }
}