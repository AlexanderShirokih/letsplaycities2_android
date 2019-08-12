package ru.aleshi.letsplaycities.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.network.NetworkViewModel
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    @MapKey
    internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(NetworkViewModel::class)
    internal abstract fun networkViewModel(viewModel: NetworkViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfirmViewModel::class)
    internal abstract fun confirmViewModel(viewModel: ConfirmViewModel): ViewModel

}