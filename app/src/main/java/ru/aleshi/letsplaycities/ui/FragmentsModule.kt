package ru.aleshi.letsplaycities.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.aleshi.letsplaycities.base.dictionary.DictionaryModule
import ru.aleshi.letsplaycities.base.game.GameModule
import ru.aleshi.letsplaycities.network.NetworkClientModule
import ru.aleshi.letsplaycities.network.NetworkModule
import ru.aleshi.letsplaycities.remote.RemoteModule
import ru.aleshi.letsplaycities.ui.blacklist.BlackListFragment
import ru.aleshi.letsplaycities.ui.friends.FriendsFragment
import ru.aleshi.letsplaycities.ui.mainmenu.MainMenuFragment
import ru.aleshi.letsplaycities.ui.network.NetworkFragment
import ru.aleshi.letsplaycities.ui.network.NetworkClientFromNetworkFragmentModule
import ru.aleshi.letsplaycities.ui.network.history.HistoryFragment
import ru.aleshi.letsplaycities.ui.remote.MultiplayerFragment
import ru.aleshi.letsplaycities.ui.remote.WaitingForDevicesFragment

@Module
abstract class FragmentsModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [GameModule::class, DictionaryModule::class])
    abstract fun contributeMainMenuFragment(): MainMenuFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [NetworkModule::class, NetworkClientFromNetworkFragmentModule::class])
    abstract fun contributeNetworkFragment(): NetworkFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [RemoteModule::class])
    abstract fun contributeWaitingForDevicesFragment(): WaitingForDevicesFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [NetworkModule::class, NetworkClientModule::class])
    abstract fun contributeFriendsFragment(): FriendsFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [NetworkModule::class, NetworkClientModule::class])
    abstract fun contributeHistoryFragment(): HistoryFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [NetworkModule::class, NetworkClientModule::class])
    abstract fun contributeBlackListFragment(): BlackListFragment
}