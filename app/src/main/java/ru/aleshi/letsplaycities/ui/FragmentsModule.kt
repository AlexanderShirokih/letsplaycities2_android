package ru.aleshi.letsplaycities.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.aleshi.letsplaycities.base.game.GameModule
import ru.aleshi.letsplaycities.base.mainmenu.MainMenuModule
import ru.aleshi.letsplaycities.network.NetworkClientModule
import ru.aleshi.letsplaycities.network.NetworkModule
import ru.aleshi.letsplaycities.remote.RemoteModule
import ru.aleshi.letsplaycities.ui.blacklist.BlackListFragment
import ru.aleshi.letsplaycities.ui.game.GameFragment
import ru.aleshi.letsplaycities.ui.network.friends.FriendsFragment
import ru.aleshi.letsplaycities.ui.mainmenu.MainMenuFragment
import ru.aleshi.letsplaycities.ui.network.NetworkFragment
import ru.aleshi.letsplaycities.ui.network.NetworkClientFromNetworkFragmentModule
import ru.aleshi.letsplaycities.ui.network.history.HistoryFragment
import ru.aleshi.letsplaycities.ui.profile.LoginProfileFragment
import ru.aleshi.letsplaycities.ui.profile.ViewProfileFragment
import ru.aleshi.letsplaycities.ui.remote.WaitingForDevicesFragment
import ru.aleshi.letsplaycities.ui.settings.SettingsFragment
import ru.aleshi.letsplaycities.ui.theme.ThemeFragment

//TODO: Create common component with NetworkModule
@Module
abstract class FragmentsModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewBindingsModule::class, MainMenuModule::class])
    abstract fun contributeMainMenuFragment(): MainMenuFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [GameModule::class])
    abstract fun contributeGameFragment(): GameFragment

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

    @ActivityScope
    @ContributesAndroidInjector(modules = [NetworkModule::class])
    abstract fun contributeFriendRequestDialog(): FriendRequestDialog

    @ActivityScope
    @ContributesAndroidInjector(modules = [NetworkModule::class])
    abstract fun contributeLoginProfileFragment(): LoginProfileFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [NetworkModule::class])
    abstract fun contributeViewProfileFragment(): ViewProfileFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeThemeFragment(): ThemeFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment
}