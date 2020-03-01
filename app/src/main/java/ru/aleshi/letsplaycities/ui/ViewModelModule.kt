package ru.aleshi.letsplaycities.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import ru.aleshi.letsplaycities.network.LpsRepositoryModule
import ru.aleshi.letsplaycities.ui.network.NetworkFetchViewModel
import ru.aleshi.letsplaycities.ui.profile.AuthorizationViewModel
import kotlin.reflect.KClass

@Module(includes = [LpsRepositoryModule::class])
abstract class ViewModelModule {

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    @MapKey
    internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(NetworkFetchViewModel::class)
    internal abstract fun fetchViewModel(viewModel: NetworkFetchViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthorizationViewModel::class)
    internal abstract fun authViewModel(viewModel: AuthorizationViewModel) : ViewModel

}